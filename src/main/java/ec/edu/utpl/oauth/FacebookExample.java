package ec.edu.utpl.oauth;
/**
 * @author jjvillavicencio
 */
import java.util.Random;
import java.util.Scanner;
import com.github.scribejava.apis.FacebookApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import java.io.IOException;

public final class FacebookExample {

    private static final String NETWORK_NAME = "Facebook";
    private static final String PROTECTED_RESOURCE_URL = "https://graph.facebook.com/v2.8/me";

    private FacebookExample() {
    }

    public static void main(String... args) throws IOException {
        final String clientId = "1063272277133283";
        final String clientSecret = "921ceebdfb1d5bc6e87f549506d9f98f";
        
        final String secretState = "secret" + new Random().nextInt(999_999);
        final OAuth20Service service = new ServiceBuilder()
                .apiKey(clientId)
                .apiSecret(clientSecret)
                .state(secretState)
                .scope("email")
                .callback("http://localhost/FB_LOGIN/")
                .build(FacebookApi.instance());

        final Scanner in = new Scanner(System.in, "UTF-8");

        System.out.println("=== " + NETWORK_NAME + "'s OAuth Workflow ===");
        System.out.println();

        // Obtener URL de autorización
        System.out.println("Obteniendo URL de autorización...");
        final String authorizationUrl = service.getAuthorizationUrl();
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
        final OAuth2AccessToken accessToken = service.getAccessToken(code);
        System.out.println("(Datos del token de acceso: " + accessToken
                + ", 'rawResponse'='" + accessToken.getRawResponse() + "')");
        System.out.println();

        // Obtener datos del usuario
        System.out.println("Ahora accedemos a los datos del usuario...");
        final OAuthRequest request = new OAuthRequest(Verb.GET, PROTECTED_RESOURCE_URL, service.getConfig());
        service.signRequest(accessToken, request);
        final Response response = request.send();
        System.out.println("Resultado de los datos...");
        System.out.println();
        System.out.println(response.getCode());
        System.out.println(response.getBody());

    }
}
