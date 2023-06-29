package com.raxim.myscoutee.profile.util;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

import org.bson.BsonType;
import org.bson.Document;
import org.bson.UuidRepresentation;
import org.bson.codecs.BsonTypeClassMap;
import org.bson.codecs.BsonValueCodecProvider;
import org.bson.codecs.Codec;
import org.bson.codecs.CollectionCodecProvider;
import org.bson.codecs.DocumentCodecProvider;
import org.bson.codecs.IterableCodecProvider;
import org.bson.codecs.MapCodecProvider;
import org.bson.codecs.ValueCodecProvider;
import org.bson.codecs.configuration.CodecRegistries;

public class AppConstants {
    public static final String WOMAN = "w";
    public static final String MAN = "m";

    public final static BsonTypeClassMap TYPE_MAP = new BsonTypeClassMap(Map.of(BsonType.BINARY, UUID.class));
    public static final Codec<Document> UUID_CODEC = CodecRegistries
            .withUuidRepresentation(CodecRegistries.fromProviders(Arrays.asList(new ValueCodecProvider(),
                    new CollectionCodecProvider(TYPE_MAP), new IterableCodecProvider(TYPE_MAP),
                    new BsonValueCodecProvider(), new DocumentCodecProvider(TYPE_MAP),
                    new MapCodecProvider(TYPE_MAP))),
                    UuidRepresentation.JAVA_LEGACY)
            .get(Document.class);

}
