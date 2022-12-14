package nl.tudelft.sem.template.cluster.integration.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * The Json util for tests.
 */
public class JsonUtil {
    /**
     * Serialize object into a string.
     *
     * @param object The object to be serialized.
     * @return A serialized string.
     * @throws JsonProcessingException if an error occurs during serialization.
     */
    public static String serialize(Object object) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return objectMapper.writeValueAsString(object);
    }

    /**
     * Deserializes a json string into an object.
     *
     * @param json The string to be deserialized.
     * @param type The type of the desired object.
     * @return The deserialized object.
     * @throws JsonProcessingException if an error occurs during deserialization.
     */
    public static <T> T deserialize(String json, Class<T> type) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(json, type);
    }
}