package com.challenge.backend.infrastructure.search.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.Instant;

@Document(indexName = "solicitations", createIndex = false)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitationDocument {
    @Id
    private String id;

    @Field(type = FieldType.Long)
    private Long solicitationId;

    @Field(type = FieldType.Long)
    private Long clientId;

    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Keyword)
    private String serviceType;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String title;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String description;

    @Field(type = FieldType.Keyword)
    private String state;

    @Field(type = FieldType.Keyword)
    private String city;

    @Field(type = FieldType.Keyword)
    private String priority;

    @Field(type = FieldType.Date)
    private Instant createdAt;

    @Field(type = FieldType.Date)
    private Instant submittedAt;
}