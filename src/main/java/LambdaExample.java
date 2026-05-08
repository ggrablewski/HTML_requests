// AWS Lambda
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

// AWS SDK v2 – Secrets Manager
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

// Java standard
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class LambdaExample implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

   private static final String API_URL = "https://api.generative.engine.capgemini.com/v2/llm/invoke";
   private static final String API_KEY = getSecret();

   private static final HttpClient httpClient =
      HttpClient.newBuilder()
         .connectTimeout(Duration.ofSeconds(10))
         .build();

   private static String getSecret() {
      String secretName = "LambdaExampleApiKey";

      Region region = Region.of(
         System.getenv().getOrDefault("AWS_REGION", "us-east-1")
      );

      try (SecretsManagerClient client = SecretsManagerClient.builder()
         .region(region)
         .build()) {

         GetSecretValueRequest request = GetSecretValueRequest.builder()
            .secretId(secretName)
            .build();

         GetSecretValueResponse response = client.getSecretValue(request);
         return response.secretString();

      } catch (Exception e) {
         throw new RuntimeException("Failed to read secret: " + secretName, e);
      }
   }

   @Override
   public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
      try {
         String payload = """
            {
              "action": "run",
              "modelInterface": "langchain",
              "data": {
                "mode": "chain",
                "text": "What is a cloud?",
                "files": [],
                "modelName": "us.anthropic.claude-sonnet-4-5-20250929-v1:0",
                "provider": "bedrock",
                "systemPrompt": "You are an assistant.",
                "modelKwargs": {
                  "maxTokens": 4096,
                  "temperature": 0.4,
                  "streaming": true,
                  "topP": 0.15
                }
              }
            }
            """;

         HttpRequest httpRequest = HttpRequest.newBuilder()
               .uri(URI.create(API_URL))
               .timeout(Duration.ofSeconds(30))
               .header("Accept", "application/json")
               .header("Content-Type", "application/json")
               .header("x-api-key", API_KEY)
               .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
               .build();

         HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

         return new APIGatewayProxyResponseEvent()
            .withStatusCode(response.statusCode())
            .withHeaders(
               java.util.Map.of(
                  "Content-Type", "application/json"
               )
            )
            .withBody(response.body());
      } catch (Exception e) {
         return new APIGatewayProxyResponseEvent()
            .withStatusCode(500)
            .withBody("""
               {
                 "error": "Internal Server Error",
                 "message": "%s"
               }
               """.formatted(e.getMessage()));
      }
   }
}
