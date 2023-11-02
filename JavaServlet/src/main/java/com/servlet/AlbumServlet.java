package com.servlet;

import com.google.gson.JsonObject;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@WebServlet(name="AlbumServlet", urlPatterns={"/AlbumStore/albums/*"})
public class AlbumServlet extends HttpServlet {


    private final String TABLE_NAME = "Albums";

    private final AwsSessionCredentials credentials = AwsSessionCredentials.create("ASIAUTRTO45LLHLHIYGZ", "WMgXsRki4IQcemArZ9+mRxG0NZ8L0gbQg9Fu1cy9", "FwoGZXIvYXdzEPf//////////wEaDIsJ/Fd/qQNU68eItyK9ATTOf4oMynR3HGzD7CROnt4mvARw+h+i5o3f0h0cg/1l4RI7eun4vkJZK86+pRwT1z99JF0ZCKfsAnbdUm5xbsHSYqMe1otEKIElJrCaoOcVNz3wMsDB7QgsbV5Uk22L2UBEM/w++Cb29Mtm6aEBj8gOQdEJyDOQ6vp4xTPakV/ek7IrX4PG81/lcUGmAOI72eb0A3xlnU3adJY/Q2M6GDgkvqih3d6n/d2oj5cHfGdW0vU22pW1ysaehyP9fCiNhYuqBjItHAbDiJXdU67ANQ9MtmJrGGHhlNQKBk+xCv0Q12LbleIWCuvNB1BmvorSystW");

    private final DynamoDbClient dynamoDbClient = DynamoDbClient.builder()
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .region(Region.US_WEST_2)
            .build();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String artist = req.getParameter("artist");
        String title = req.getParameter("title");
        String year = req.getParameter("year");
        String image = req.getParameter("image");

        String albumID = UUID.randomUUID().toString();

        Map<String, AttributeValue> item = new HashMap<>();
        item.put("albumID", AttributeValue.builder().s(albumID).build());
        item.put("artist", AttributeValue.builder().s(artist).build());
        item.put("title", AttributeValue.builder().s(title).build());
        item.put("year", AttributeValue.builder().s(year).build());
        item.put("image", AttributeValue.builder().s(image).build());

        PutItemRequest putItemRequest = PutItemRequest.builder()
                .tableName(TABLE_NAME)
                .item(item)
                .build();


        dynamoDbClient.putItem(putItemRequest);

        JsonObject responseJson = new JsonObject();
        responseJson.addProperty("albumID", albumID);
        responseJson.addProperty("image", image);

        resp.setContentType("application/json");
        resp.getWriter().write(responseJson.toString());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String[] paths = req.getPathInfo().split("/");
        String albumID = paths[paths.length - 1];

        GetItemRequest getItemRequest = GetItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(Map.of("albumID", AttributeValue.builder().s(albumID).build()))
                .build();


        GetItemResponse getItemResponse = dynamoDbClient.getItem(getItemRequest);

        Map<String, AttributeValue> retrievedItem = getItemResponse.item();

        if (retrievedItem == null || retrievedItem.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
            return;
        }

        JsonObject albumInfo = new JsonObject();
        albumInfo.addProperty("artist", retrievedItem.get("artist").s());
        albumInfo.addProperty("title", retrievedItem.get("title").s());
        albumInfo.addProperty("year", retrievedItem.get("year").s());
        albumInfo.addProperty("image", retrievedItem.get("image").s());


        resp.setContentType("application/json");
        resp.getWriter().write(albumInfo.toString());
    }
}
