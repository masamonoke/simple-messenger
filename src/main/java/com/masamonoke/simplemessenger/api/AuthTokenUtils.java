package com.masamonoke.simplemessenger.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class AuthTokenUtils {
    public static String getTokenFromHeader(String header) {
        var tokenStartIdx = 7;
        return header.substring(tokenStartIdx);
    }
    @SuppressWarnings("unchecked")
    public static Map<String, String> decodeToken(String token) throws JsonProcessingException {
        String[] chunks = token.split("\\.");
        Base64.Decoder decoder = Base64.getUrlDecoder();
        String payload = new String(decoder.decode(chunks[1]));
        var om = new ObjectMapper();
        return om.readValue(payload, HashMap.class);
    }

}
