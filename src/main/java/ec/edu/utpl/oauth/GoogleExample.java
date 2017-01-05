package ec.edu.utpl.oauth;
/**
 * @author jjvillavicencio
 */
import java.util.Random;
import java.util.Scanner;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.apis.GoogleApi20;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class GoogleExample {

    private static final String NETWORK_NAME = "G+";
    private static final String PROTECTED_RESOURCE_URL = "https://www.googleapis.com/plus/v1/people/me";

    private GoogleExample() {
    }

    public static void main(String... args) throws IOException {
        final String clientId = "667536381388-jjirdpf16kcdq0cltj7qltksn50ejs9o."
                + "apps.googleusercontent.com";
        final String clientSecret = "KiAripa_pqN0ef9H2-_h4ybh";
        
        final String secretState = "secret" + new Random().nextInt(999_999);
        final OAuth20Service service = new ServiceBuilder()
                .apiKey(clientId)
                .apiSecret(clientSecret)
                .scope("profile") // replace with desired scope
                .state(secretState)
                .callback("http://localhost/FB_LOGIN/")
                .build(GoogleApi20.instance());
        final Scanner in = new Scanner(System.in, "UTF-8");

        System.out.println("=== " + NETWORK_NAME + "'s OAuth Workflow ===");
        System.out.println();

        // Obtener URL de autorización
        System.out.println("Obteniendo URL de autorización...");
        //pass access_type=offline to get refresh token
        //https://developers.google.com/identity/protocols/OAuth2WebServer#preparing-to-start-the-oauth-20-flow
        final Map<String, String> additionalParams = new HashMap<>();
        additionalParams.put("access_type", "offline");
        //force to reget refresh token (if usera are asked not the first time)
        additionalParams.put("prompt", "consent");
        final String authorizationUrl = service.getAuthorizationUrl(additionalParams);
        System.out.println("Ve a la URL de autorización:");
        System.out.println(authorizationUrl);
        System.out.println("Pega el código de autorización aquí:");
        System.out.print(">>");
        final String code = in.nextLine();
        System.out.println();

        System.out.println("Pega el estado del servidor aqui:");
        System.out.print(">>");
        final String value = in.nextLine();
        if (secretState.equals(value)) {
            System.out.println("Valor del estado coincidió!");
        } else {
            System.out.println("Valor del estado no coincidió!");
            System.out.println("valor esperado = " + secretState);
            System.out.println("valor obtenido      = " + value);
            System.out.println();
        }

        // Solicitar token de acceso y verficarlo
        System.out.println("Solicitando token de acceso...");
        OAuth2AccessToken accessToken = service.getAccessToken(code);
        System.out.println("(Datos del token de acceso: " + accessToken
                + ", 'rawResponse'='" + accessToken.getRawResponse() + "')");

        System.out.println("Refreshing the Access Token...");
        accessToken = service.refreshAccessToken(accessToken.getRefreshToken());
        System.out.println("Refreshed the Access Token!");
        System.out.println("(if your curious it looks like this: " + accessToken
                + ", 'rawResponse'='" + accessToken.getRawResponse() + "')");
        System.out.println();

        // Obtener datos del usuario
        System.out.println("Ahora accedemos a los datos del usuario...");
        while (true) {
            System.out.println("Pegue los campos a buscar (deje en blanco para obtener todo el perfil, 'exit' para detener)");
            System.out.print(">>");
            final String query = in.nextLine();
            System.out.println();

            final String requestUrl;
            if ("exit".equals(query)) {
                break;
            } else if (query == null || query.isEmpty()) {
                requestUrl = PROTECTED_RESOURCE_URL;
            } else {
                requestUrl = PROTECTED_RESOURCE_URL + "?fields=" + query;
            }
            
            System.out.println("Ahora accedemos a los datos del usuario...");
            final OAuthRequest request = new OAuthRequest(Verb.GET, requestUrl, service.getConfig());
            service.signRequest(accessToken, request);
            final Response response = request.send();
            System.out.println("Resultado de los datos...");
            System.out.println();
            System.out.println(response.getCode());
            System.out.println(response.getBody());
        }
    }
}
