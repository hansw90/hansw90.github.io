package com.example.test.contorller;

import com.example.test.dto.TestDto;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("test")
public class TestContorller {


    /**
     * 단순 스트링에 대한 리턴
     * @return
     */
    @PostMapping("return/string")
    public String test() {
        TestDto testDto = new TestDto("한승우", 1, new TestDto.TestChild("테스트")
        , null, null, null);
        Gson gson = new Gson().newBuilder().serializeNulls().create();

        String testJson = gson.toJson(testDto);
        return testJson;
    }

    /**
     * ResponseEntity에 담아 리턴
     * @return
     */
    @PostMapping("return/responseEntity/toJson")
    public ResponseEntity<?> test2() {
        TestDto testDto = new TestDto("한승우", 1, new TestDto.TestChild("테스트")
                , null, null, null);
        Gson gson = new Gson().newBuilder().serializeNulls().create();

        String testJson = gson.toJson(testDto);
        return ResponseEntity.status(HttpStatus.OK).body(testJson);
    }


    /**
     * ResponseEntity에 JsonObject로 파싱된 값을 담아 리턴
     * @return
     */
    @PostMapping("return/responseEntity/toJsonTree")
    public ResponseEntity<?> test3() {
        TestDto testDto = new TestDto("한승우", 1, new TestDto.TestChild("테스트")
                , null, null, null);
        Gson gson = new Gson().newBuilder().serializeNulls().create();
        String testString = gson.toJson(testDto);
        JsonObject jsonObject = new JsonObject();
        JsonElement testJson = gson.toJsonTree(testDto);
        JsonElement testStringToJsonElement = gson.toJsonTree(testString);
        jsonObject.add("test1", testJson);
        jsonObject.add("test2", testStringToJsonElement);

        return ResponseEntity.status(HttpStatus.OK).body(jsonObject);
    }


    @PostMapping("return/responseEntity/toJsonTree2")
    public ResponseEntity<?> test4() {
        String json = "{\"a\": 1, \"b\": 1}";
        Gson gson = new Gson().newBuilder().serializeNulls().create();
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);

        System.out.println(jsonObject); // {"a":1,"b":1}

        jsonObject.addProperty("a", 2);
        jsonObject.add("b", JsonNull.INSTANCE);
        System.out.println(jsonObject); // {"a":2,"b":null}

//        jsonObject.remove("a");
        System.out.println(jsonObject.toString()); // {"b":null}
        ResponseEntity responseEntity = ResponseEntity.status(HttpStatus.OK).body(jsonObject.toString());
        System.out.println(responseEntity.getBody().toString());


        return ResponseEntity.status(HttpStatus.OK).body(jsonObject.toString());
    }
}
