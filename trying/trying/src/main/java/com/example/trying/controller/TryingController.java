package com.example.trying.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.trying.ExceptionHandler.CustomeApiNameException;
import com.example.trying.services.JsonService;
import com.example.trying.utils.FinalVariables;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;

import okhttp3.FormBody;
import okhttp3.Request;

@RestController
public class TryingController {
	private JsonService data;

	@Autowired
	public TryingController(JsonService data) {
		this.data = data;
	}
	/**
	 * createMobileNumber method is used during the post call of createWallet to
	 * check the request and to send the particular response to the user.
	 * 
	 * @param name
	 * @return
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	@PostMapping("/{uri}")
	public ResponseEntity<JsonNode> createMobileNumber(@PathVariable String uri,@RequestBody String requestMessage)
			throws ClassNotFoundException, IOException {
		List<String> checker = data.rootValues();
		String apiName;
		if(checker.contains(uri)) {
			apiName = uri;
		}
		else
		{
			return null;
		}
		JsonNode response = data.validateAndGetResponse(requestMessage, apiName);
		if (response != null) {
			if (response.get("Response").get("Status").asText().toLowerCase().equals("success")) {
				JsonNode detail = data.details(FinalVariables.CREATE_WALLET_APINAME);
				multiResponse(FinalVariables.CREATE_WALLET_APINAME, detail, requestMessage);
				return ResponseEntity.ok(response);
			} else {
				return ResponseEntity.ok(response);
			}
		} else {
			throw new CustomeApiNameException("There is no such apiName name you provided is present ");
		}

	}

	@PostMapping("/createCard")
	public ResponseEntity<JsonNode> createCard(@RequestBody String requestMessage)
			throws ClassNotFoundException, IOException {
		JsonNode response = data.validateAndGetResponse(requestMessage, FinalVariables.CREATE_CARD_APINAME);
		if (response != null) {
			if (response.get("Response").get("Status").asText().toLowerCase().equals("success")) {
				JsonNode detail = data.details(FinalVariables.CREATE_CARD_APINAME);
				multiResponse(FinalVariables.CREATE_CARD_APINAME, detail, requestMessage);
				return ResponseEntity.ok(response);
			} else {
				return ResponseEntity.ok(response);
			}
		} else {
			throw new CustomeApiNameException("There is no such apiName name you provided is present ");
		}

	}

	/**
	 * This method is used for the call back url process with multiple threads.
	 * 
	 * @param apiName
	 * @param detail
	 * @param requestBodyField
	 * @throws JsonMappingException
	 * @throws JsonProcessingException
	 */
	public void multiResponse(String apiName, JsonNode detail, String requestBodyField)
			throws JsonMappingException, JsonProcessingException {
		if (detail.get(FinalVariables.callBackUrl) != null) {
			JsonNode backUrl = detail.get(FinalVariables.callBackUrl);
			for (JsonNode a : backUrl) {
				String url = a.get(FinalVariables.url).asText();
				String requestMessage = a.get("RequestMessage").asText();
				Thread asyncThread = new Thread(() -> {
					try {
						okhttp3.RequestBody requestBody = new FormBody.Builder().add("requestMessage", requestMessage)
								.build();
						Request request = new Request.Builder().url(url).post(requestBody).build();
						data.asyncRequest(request, a);
					} catch (IOException e) {
						e.printStackTrace();
					}
				});
				asyncThread.start();
			}
		} else {
// No Callback url was configured
		}
	}

	/**
	 * getMobileNumber method is used during the post call of getWallet to check the
	 * request and to send the particular response to the user.
	 * 
	 * @param name
	 * @return
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	@PostMapping("/getWallet")
	public ResponseEntity<JsonNode> getMobileNumber(@RequestBody String requestBodyField)
			throws ClassNotFoundException, IOException {
		JsonNode response = data.validateAndGetResponse(requestBodyField, FinalVariables.GET_WALLET_APINAME);
		if (response != null) {
			return ResponseEntity.ok(response);
		} else {
			throw new com.example.trying.ExceptionHandler.CustomeApiNameException(
					"There is no such apiName name you provided is present ");
		}
	}

	/**
	 * refreshDetails is used to reload the json file details again after the
	 * changes made in json file.
	 * 
	 * @return
	 */
	@PostMapping("/refresh")
	public ResponseEntity<Map<String, String>> refreshDetails() {

		Map<String, String> response = new HashMap<>();
		data.initialize();
		response.put("Status", "00");
		response.put("Description", "Success");
		return ResponseEntity.ok(response);
	}
}