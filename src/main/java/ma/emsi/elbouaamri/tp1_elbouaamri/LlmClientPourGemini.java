package ma.emsi.elbouaamri.tp1_elbouaamri;

import jakarta.enterprise.context.Dependent;
import jakarta.ws.rs.client.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.Serializable;
import java.util.logging.Logger;

/**
 * Gère l'interface avec l'API de Gemini.
 * Son rôle est essentiellement de lancer une requête à chaque nouvelle
 * question qu'on veut envoyer à l'API.
 *
 * De portée dependent pour réinitialiser la conversation à chaque fois que
 * l'instance qui l'utilise est renouvelée.
 */
@Dependent
public class LlmClientPourGemini implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(LlmClientPourGemini.class.getName());
    // Clé pour l'API du LLM
    private final String key;

    // Client REST. Facilite les échanges avec une API REST.
    private Client clientRest;

    // Représente un endpoint de serveur REST
    private final WebTarget target;

    /**
     * Constructeur : récupère la clé API et initialise le client REST
     */
    public LlmClientPourGemini(String texteRequeteJson, String texteReponseJson, String s) {
        // Récupère la clé secrète depuis la variable d'environnement
        this.key = System.getenv("GEMINI_KEY");

        System.out.println("=== DEBUG GEMINI CLIENT ===");
        System.out.println("GEMINI_KEY présente : " + (this.key != null && !this.key.isEmpty()));
        if (this.key != null && !this.key.isEmpty()) {
            System.out.println("Longueur clé : " + this.key.length());
            System.out.println("Début clé : " + this.key.substring(0, Math.min(10, this.key.length())) + "...");
            System.out.println("Format clé valide : " + this.key.startsWith("AIza"));
        } else {
            System.err.println("ERREUR : GEMINI_KEY est NULL ou vide !");
            System.err.println("Variables d'environnement disponibles contenant 'KEY' ou 'GEMINI' :");
            System.getenv().keySet().forEach(k -> {
                if (k.toUpperCase().contains("GEMINI") || k.toUpperCase().contains("KEY")) {
                    System.err.println("  - " + k);
                }
            });
        }

        if (this.key == null || this.key.isEmpty()) {
            throw new IllegalStateException(
                    "La variable d'environnement GEMINI_KEY n'est pas définie. " +
                            "Veuillez la définir avec votre clé API Gemini."
            );
        }

        // Vérification du format de la clé
        if (!this.key.startsWith("AIza")) {
            System.err.println("ATTENTION : La clé API ne commence pas par 'AIza'. " +
                    "Assurez-vous d'utiliser une clé API Google valide.");
        }

        // Client REST pour envoyer des requêtes vers l'API
        this.clientRest = ClientBuilder.newClient();

        // API GRATUITE - Modèles disponibles selon la liste de votre compte

        // Option 1 : gemini-2.5-flash (RECOMMANDÉ - Le plus récent, juin 2025)
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";
        // Option 2 : gemini-2.0-flash (Stable, janvier 2025)
        // String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";

        // Option 3 : gemini-flash-latest (Pointe vers la dernière version)
        // String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent";

        // Tous ces modèles sont GRATUITS et supportent generateContent

        // Création du WebTarget avec la clé API
        this.target = clientRest.target(url).queryParam("key", this.key);

        System.out.println("URL configurée : " + url);
        System.out.println("Modèle utilisé : " + extractModelName(url));
        System.out.println("Version API : " + extractApiVersion(url));
        System.out.println("===========================");
    }

    /**
     * Envoie une requête à l'API de Gemini.
     *
     * @param requestEntity le corps de la requête (en JSON).
     * @return réponse REST de l'API (corps en JSON).
     */
    public Response envoyerRequete(Entity requestEntity) {
        System.out.println("=== ENVOI REQUÊTE À GEMINI ===");

        Invocation.Builder request = target.request(MediaType.APPLICATION_JSON_TYPE);

        // Envoie la requête POST au LLM
        Response response = request.post(requestEntity);

        System.out.println("Status HTTP reçu : " + response.getStatus());
        System.out.println("Status Info : " + response.getStatusInfo());
        System.out.println("==============================");
        return response;
    }

    /**
     * Ferme le client REST proprement
     */
    public void closeClient() {
        if (this.clientRest != null) {
            this.clientRest.close();
        }
    }

    /**
     * Extrait le nom du modèle de l'URL
     */
    private String extractModelName(String url) {
        int start = url.indexOf("/models/") + 8;
        int end = url.indexOf(":", start);
        return end > start ? url.substring(start, end) : "inconnu";
    }

    /**
     * Extrait la version de l'API de l'URL
     */
    private String extractApiVersion(String url) {
        if (url.contains("/v1beta/")) {
            return "v1beta";
        } else if (url.contains("/v1/")) {
            return "v1";
        }
        return "inconnue";
}
}






