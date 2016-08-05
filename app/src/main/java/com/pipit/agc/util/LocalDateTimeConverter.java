package com.pipit.agc.util;

/*
 * GSON Joda Time Serialisers
 *
 * Copyright 2013-2015 Greg Kopff
 * All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */


        import com.google.gson.JsonDeserializationContext;
        import com.google.gson.JsonDeserializer;
        import com.google.gson.JsonElement;
        import com.google.gson.JsonParseException;
        import com.google.gson.JsonPrimitive;
        import com.google.gson.JsonSerializationContext;
        import com.google.gson.JsonSerializer;
        import org.joda.time.LocalDateTime;
        import org.joda.time.format.DateTimeFormat;
        import org.joda.time.format.DateTimeFormatter;

        import java.lang.reflect.Type;

/**
 * GSON serialiser/deserialiser for converting Joda {@link LocalDateTime} objects.
 */
public class LocalDateTimeConverter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime>
{
    /** Format specifier */
    private static final String PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS";

    /**
     * Gson invokes this call-back method during serialization when it encounters a field of the
     * specified type. <p>
     *
     * In the implementation of this call-back method, you should consider invoking
     * {@link JsonSerializationContext#serialize(Object, Type)} method to create JsonElements for any
     * non-trivial field of the {@code src} object. However, you should never invoke it on the
     * {@code src} object itself since that will cause an infinite loop (Gson will call your
     * call-back method again).
     * @param src the object that needs to be converted to Json.
     * @param typeOfSrc the actual type (fully genericized version) of the source object.
     * @return a JsonElement corresponding to the specified object.
     */
    @Override
    public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context)
    {
        final DateTimeFormatter fmt = DateTimeFormat.forPattern(PATTERN);
        return new JsonPrimitive(fmt.print(src));
    }

    /**
     * Gson invokes this call-back method during deserialization when it encounters a field of the
     * specified type. <p>
     *
     * In the implementation of this call-back method, you should consider invoking
     * {@link JsonDeserializationContext#deserialize(JsonElement, Type)} method to create objects
     * for any non-trivial field of the returned object. However, you should never invoke it on the
     * the same type passing {@code json} since that will cause an infinite loop (Gson will call your
     * call-back method again).
     * @param json The Json data being deserialized
     * @param typeOfT The type of the Object to deserialize to
     * @return a deserialized object of the specified type typeOfT which is a subclass of {@code T}
     * @throws JsonParseException if json is not in the expected format of {@code typeOfT}
     */
    @Override
    public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException
    {
        // Do not try to deserialize null or empty values
        if (json.getAsString() == null || json.getAsString().isEmpty())
        {
            return null;
        }

        final DateTimeFormatter fmt = DateTimeFormat.forPattern(PATTERN);
        return fmt.parseLocalDateTime(json.getAsString());
    }
}