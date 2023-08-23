package com.example.trying.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.trying.ExceptionHandler.CustomeApiNameException;
import com.example.trying.ExceptionHandler.CustomeSuccessException;
import com.example.trying.ExceptionHandler.CustomerFailureException;
import com.example.trying.ExceptionHandler.CustomerFailureListException;
import com.example.trying.ExceptionHandler.FailureTestOnException;
import com.example.trying.utils.FinalVariables;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import jakarta.annotation.PostConstruct;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

@Service
public class JsonService {
	private static final ObjectMapper obj = new ObjectMapper();
	private Map<String, JsonNode> walletCache = new HashMap<>();
	private  List<String> rootList= new ArrayList<>();
	private static String jsonFile = System.getenv("SWITCH_ON_JSON");
	private static final Logger logger = LoggerFactory.getLogger(JsonService.class);

	/**
	 * This method is used to pre load the json file data and to store in cache
	 * variable.
	 * 
	 * @throws ParseException
	 */
	@PostConstruct
	public void initialize() {
		try {
			byte[] resource = Files.readAllBytes(Paths.get(jsonFile));
			String jsonData = new String(resource);
			JsonNode root = obj.readTree(jsonData);
			rootList = provideListOfKeys(root);
			Iterator<Entry<String, JsonNode>> customerEntries = root.fields();
			while (customerEntries.hasNext()) {
				Map.Entry<String, JsonNode> customerEntry = customerEntries.next();
				String key = customerEntry.getKey();
				JsonNode value = customerEntry.getValue();
				walletCache.put(key, value);
			}
		} catch (IOException e) {
// TODO Auto-generated catch block
			e.printStackTrace();
			throw new CustomeApiNameException("There is no json file.");
		}
	}

		public List<String> rootValues()
		{
			return rootList;
		}
	/**
	 * This method is used to call async request om okhttp client during the success
	 * response.
	 * 
	 * @param request
	 * @throws IOException
	 */
	public void asyncRequest(Request request, JsonNode backUrl) throws IOException {
		logger.info("CAll back Request Inititated !!");
		OkHttpClient client = new OkHttpClient();
		logger.warn("Url -- " + backUrl.get("Url").asText());
		logger.warn("RequestMessage -- " + backUrl.get("RequestMessage").asText());
		try (Response response = client.newCall(request).execute()) {
			ResponseBody responseBody = (ResponseBody) response.body();
			if (responseBody != null) {
				logger.warn("Status -- " + String.valueOf(response.code()));
//logger.warn("Response -- " + responseBody.string());
			} else {
				logger.warn("no proper request call occur...");
			}
		}
	}

	/**
	 * This Method is used to validate the mobile number from the request
	 * 
	 * @param mobileNumber
	 * @param apiName
	 * @return JsonNode
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public JsonNode validateAndGetResponse(String paramRequestMessage, String paramApiName) {
		JsonNode config = walletCache.get(paramApiName);
		if (config != null) {
			if (config.has(FinalVariables.failureTestOn)) {
				String failureTestOn = config.get(FinalVariables.failureTestOn).asText();
				JsonNode failureTestList = walletCache.get(FinalVariables.failureTestList);
				if (failureTestList.has(failureTestOn)) {
					JsonNode requestJson;
					try {
						requestJson = obj.readTree(paramRequestMessage);
						List<String> failureDataList = obj.readValue(
								walletCache.get(FinalVariables.failureTestList).get(failureTestOn).toString(),
								new TypeReference<>() {
								});
						String requestFieldValue = requestJson.get(failureTestOn).asText();
						boolean isFailure = failureDataList.contains(requestFieldValue);
						if (isFailure) {
							return apiFailure(paramApiName);
						} else {
							return apiSuccess(paramApiName, requestJson);
						}
					} catch (JsonProcessingException e) {
// TODO Auto-generated catch block
						e.printStackTrace();
						throw new CustomerFailureListException("The list of data retriving failed");
					}

				} else {
					throw new FailureTestOnException("There is no failer test on..");
				}
			} else {
				return config.get(FinalVariables.success);
			}
		} else {
			throw new CustomeApiNameException("There is no such api name in the configuration !!");
		}
	}

	/**
	 * This method is used to give the JsonNode response of the failure case.
	 * 
	 * @param apiName
	 * @return
	 */
	private JsonNode apiFailure(String paramApiName) {
		JsonNode failureResponse = walletCache.get(paramApiName).get(FinalVariables.failure);
		if (failureResponse != null) {
			return failureResponse;
		} else {
			throw new CustomerFailureException("There is no failure field in the apiname.");
		}
	}

	/**
	 * This method is used to give the JsonNode response of the success case.
	 * 
	 * @param apiName
	 * @param failureTest
	 * @param req
	 * @return
	 */
	private JsonNode apiSuccess(String paramApiName, JsonNode paramRequestJson) {
		JsonNode successResponse = walletCache.get(paramApiName).get(com.example.trying.utils.FinalVariables.success);
		if (successResponse == null) {
			throw new CustomeSuccessException("There is no success field in the paritcular apiname.");
		} else {
			List<String> requestMessageList = provideListOfKeys(paramRequestJson);
			List<String> successMessageList = provideListOfKeys(successResponse);
			List<String> filteredList = new ArrayList<>();
			ListIterator<String> requestMessageIterator = requestMessageList.listIterator();
			while (requestMessageIterator.hasNext()) {
				String val = requestMessageIterator.next();
				if (successMessageList.contains(val)) {
					filteredList.add(val);
				}
			}
			List<String> echoList = new ArrayList<>();
			JsonNode echo = walletCache.get(paramApiName).get(com.example.trying.utils.FinalVariables.echoBack);
			for (JsonNode field : echo) {
				String value = field.asText();
				if (filteredList.contains(value)) {
					echoList.add(value);
				}
			}
			JsonNode answer = null;
			if (!echoList.isEmpty()) {
				answer = apiSuccessEchoBack(successResponse, echoList, paramApiName, paramRequestJson, filteredList);
			} else {
				answer = randomNumberGenerator(successResponse, paramApiName, filteredList);
			}
			return answer;

		}
	}

	public List<String> provideListOfKeys(JsonNode paramRequestJson) {
		List<String> temp = new ArrayList<>();
		Iterator<String> reqBodykeyNames = paramRequestJson.fieldNames();
		reqBodykeyNames.forEachRemaining(e -> temp.add(e));
		return temp;
	}

	/**
	 * This method is used to generate a EchoBack the request field values details
	 * witch the success response.
	 * 
	 * @param ecoResponse
	 * @param ecoList
	 * @param apiName
	 * @param req
	 * @return apiSuccessEchoBack
	 */
	private JsonNode apiSuccessEchoBack(JsonNode paramSuccessResponse, List<String> paramEchoList, String paramApiName,
			JsonNode req, List<String> filteredArray) {
		if (!paramEchoList.isEmpty()) {
			for (int i = 0; i < paramEchoList.size(); i++) {
				String key = paramEchoList.get(i);
				((ObjectNode) paramSuccessResponse).put(key, req.get(key).asText());
			}
			paramSuccessResponse = randomNumberGenerator(paramSuccessResponse, paramApiName, filteredArray);
		}
		return paramSuccessResponse;
	}

	/**
	 * This method is used when EhocBack is not having the request field key to
	 * generate random numbers for the particular key.
	 * 
	 * @param ecoResponse
	 * @param apiName
	 * @param filteredArray
	 * @return
	 */
// Change the function name
	private JsonNode randomNumberGenerator(JsonNode paramSuccessResponse, String paramApiName,
			List<String> filteredArray) {
		JsonNode randomFields = walletCache.get(paramApiName).get(FinalVariables.randomFields);
		for (JsonNode field : randomFields) {
			String key = field.get("Tag").asText();
			String value = getRandomNumber(paramApiName, key, field);
			((ObjectNode) paramSuccessResponse).put(key, value);
		}
		return paramSuccessResponse;
	}

	/**
	 * This method is used to send the details of particular apiName during the
	 * okHttp request check in the controller.
	 * 
	 * @param apiName
	 * @return
	 */
	public JsonNode details(String paramApiName) {
		JsonNode a = walletCache.get(paramApiName);
		return a;
	}

	/**
	 * This method is used to return random number for the particular fields.
	 * 
	 * @return String
	 */

	private String getRandomNumber(String paramApiName, String fieldName, JsonNode randomField) {
		JsonNode randomFields = randomField;
		int length = randomFields.get("Length").asInt();
		String prefix = randomFields.get("Prefix").asText();
		String tag = generateRandom(length - (prefix.length() - 1));
		return prefix + tag;
	}

	/**
	 * This method is used to generate the random number based on the given length.
	 * in the parameter.
	 * 
	 * @param length
	 * @return String
	 */
	private String generateRandom(int length) {
		Random random = new Random();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++) {
			sb.append(random.nextInt(10));
		}
		return sb.toString();
	}

}
