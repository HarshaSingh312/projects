package org.example.mapper;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.math.BigDecimal;

public class AmountDeserializer2 extends JsonDeserializer<BigDecimal> {

    @Override
    public BigDecimal deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        String rawAmount = p.getText();
        if (rawAmount.isBlank()) return null;
        String amountParts = rawAmount.replace("$", "").replace(",", "");
        return new BigDecimal(amountParts);
    }
}
