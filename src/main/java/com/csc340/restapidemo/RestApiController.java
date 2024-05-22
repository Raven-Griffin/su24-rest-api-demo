package com.csc340.restapidemo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;



import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
public class RestApiController {

    private static final String FILE_PATH = "students.json";
    private ObjectMapper objectMapper = new ObjectMapper();

    private Map<Integer, Student> studentDatabase = new HashMap<>();

    public RestApiController() {
        loadStudentsFromJson();
    }

    private void loadStudentsFromJson() {
        try {
            Path filePath = Paths.get(FILE_PATH);

// make sure file exists
            if (Files.exists(filePath)) {
                String jsonContent = Files.readString(filePath);
                studentDatabase = objectMapper.readValue(jsonContent, new TypeReference<Map<Integer, Student>>() {});
            }

            //sends a message
        } catch (IOException e) {
            Logger.getLogger(RestApiController.class.getName())
                    .log(Level.SEVERE, "Failed to load", e);
        }
    }

    private void saveToFile() {
        // Initialize a FileWriter
        try (FileWriter fileWriter = new FileWriter(FILE_PATH)) {
            objectMapper.writeValue(fileWriter, studentDatabase);
        } catch (IOException e) {
            Logger.getLogger(RestApiController.class.getName())
                    .log(Level.SEVERE, "Failed to save students to file", e);
        }
    }
    /**
     * Hello World API endpoint.
     *
     * @return response string.
     */

    @GetMapping("/hello")
    public String hello() {
        return "Hello, World!";
    }

    /**
     * Greeting API endpoint.
     *
     * @param name the request parameter
     * @return the response string.
     */


    @GetMapping("/greeting")
    public String greeting(@RequestParam(value = "name", defaultValue = "Dora") String name) {
        return "Hola, soy " + name;
    }
 /**
     * List all students.
     *
     * @return the list of students.
     */
    @GetMapping("students/all")
    public Object getAllStudents() {
        if (studentDatabase.isEmpty()) {
            studentDatabase.put(1, new Student(1, "sample1", "csc", 3.86));
        }
        return studentDatabase.values();
    }
    /**
     * Get one student by Id
     *
     * @param id the unique student id.
     * @return the student.
     */
    @GetMapping("students/{id}")
    public Student getStudentById(@PathVariable int id) {
        return studentDatabase.get(id);
    }


    @PostMapping("students/create")
    public Object createStudent(@RequestBody Student student) {
        studentDatabase.put(student.getId(), student);
        //save
        saveToFile();
        return studentDatabase.values();
    }

    @PutMapping("students/update/{id}")
    public Object updateStudent(@PathVariable int id, @RequestBody Student updatedStudent) {
        // checks to see if IDs match
        if (!studentDatabase.containsKey(id)) {
            return "Student " + id + " does not exist.";
        }
        updatedStudent.setId(id);
        studentDatabase.put(id, updatedStudent);
        saveToFile();

        return "Student update successful!";
    }

    /**
     * Delete a Student by id
     *
     * @param id the id of student to be deleted.
     * @return the List of Students.
     */
    @DeleteMapping("students/delete/{id}")
    public Object deleteStudent(@PathVariable int id) {
        //if else that make sure the id exists the deletes
        if (!studentDatabase.containsKey(id)) {
            return "Student " + id + " doesn't exist.";
        }else {
            studentDatabase.remove(id);
            saveToFile();
            return studentDatabase.values();
        }
    }


    /**
     * Get a quote from quotable and make it available our own API endpoint
     *
     * @return The quote json response
     */
    @GetMapping("/quote")
    public Object getQuote() {
        try {
            String url = "https://api.quotable.io/random";
            RestTemplate restTemplate = new RestTemplate();
            ObjectMapper mapper = new ObjectMapper();

            //We are expecting a String object as a response from the above API.
            String jSonQuote = restTemplate.getForObject(url, String.class);
            JsonNode root = mapper.readTree(jSonQuote);

            //Parse out the most important info from the response and use it for whatever you want. In this case, just print.
            String quoteAuthor = root.get("author").asText();
            String quoteContent = root.get("content").asText();
            System.out.println("Author: " + quoteAuthor);
            System.out.println("Quote: " + quoteContent);

            return root;

        } catch (JsonProcessingException ex) {
            Logger.getLogger(RestApiController.class.getName()).log(Level.SEVERE,
                    null, ex);
            return "error in /quote";
        }
    }

    /**
     * Get a list of universities from hipolabs and make them available at our own API
     * endpoint.
     *
     * @return json array
     */
    @GetMapping("/univ")
    public Object getUniversities() {
        try {
            String url = "http://universities.hipolabs.com/search?name=sports";
            RestTemplate restTemplate = new RestTemplate();
            ObjectMapper mapper = new ObjectMapper();

            String jsonListResponse = restTemplate.getForObject(url, String.class);
            JsonNode root = mapper.readTree(jsonListResponse);

            //The response from the above API is a JSON Array, which we loop through.
            for (JsonNode rt : root) {
                //Extract relevant info from the response and use it for what you want, in this case just print to the console.
                String name = rt.get("name").asText();
                String country = rt.get("country").asText();
                System.out.println(name + ": " + country);
            }

            return root;
        } catch (JsonProcessingException ex) {
            Logger.getLogger(RestApiController.class.getName()).log(Level.SEVERE,
                    null, ex);
            return "error in /univ";
        }

    }
    /**
     * Get a list of countries and make them available at our own API
     * endpoint.
     *
     * @return json array
     */
    @GetMapping(value ="/countries")
    public List<Object> getCountries(){
        String url="https://apiv3.iucnredlist.org/api/v3/docs#countries-species";
        RestTemplate restTemplate = new RestTemplate();

        String countries = restTemplate.getForObject(url, String.class);

        List<Object> list = Arrays.asList(countries);
        return list;
    }

}