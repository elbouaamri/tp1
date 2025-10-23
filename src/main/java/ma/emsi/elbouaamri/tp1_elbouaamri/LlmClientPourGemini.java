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
    public LlmClientPourGemini() {
        // Récupère la clé secrète depuis la variable d'environnement
        this.key = System.getenv("GEMINI_KEY");

        LOGGER.info("=== DEBUG GEMINI CLIENT ===");
        LOGGER.info("GEMINI_KEY présente : " + (this.key != null && !this.key.isEmpty()));
        if (this.key != null && !this.key.isEmpty()) {
            LOGGER.info("Longueur clé : " + this.key.length());
            LOGGER.info("Début clé : " + this.key.substring(0, Math.min(10, this.key.length())) + "...");
            LOGGER.info("Format clé valide : " + this.key.startsWith("AIza"));
        } else {
            LOGGER.severe("ERREUR : GEMINI_KEY est NULL ou vide !");
            LOGGER.severe("Variables d'environnement disponibles contenant 'KEY' ou 'GEMINI' :");
            System.getenv().keySet().forEach(k -> {
                if (k.toUpperCase().contains("GEMINI") || k.toUpperCase().contains("KEY")) {
                    LOGGER.severe("  - " + k);
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
            LOGGER.warning("ATTENTION : La clé API ne commence pas par 'AIza'. " +
                    "Assurez-vous d'utiliser une clé API Google valide.");
        }

        // Client REST pour envoyer des requêtes vers l'API
        this.clientRest = ClientBuilder.newClient();

        // API GRATUITE - Modèles disponibles selon la liste de votre compte
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";
        this.target = clientRest.target(url).queryParam("key", this.key);

        LOGGER.info("URL configurée : " + url);
        LOGGER.info("Modèle utilisé : " + extractModelName(url));
        LOGGER.info("Version API : " + extractApiVersion(url));
        LOGGER.info("===========================");
    }

    /**
     * Envoie une requête à l'API de Gemini.
     *
     * @param requestEntity le corps de la requête (en JSON).
     * @return réponse REST de l'API (corps en JSON).
     */
    public Response envoyerRequete(Entity requestEntity) {
        LOGGER.info("=== ENVOI REQUÊTE À GEMINI ===");
        Invocation.Builder request = target.request(MediaType.APPLICATION_JSON_TYPE);
        Response response = request.post(requestEntity);
        LOGGER.info("Status HTTP reçu : " + response.getStatus());
        LOGGER.info("Status Info : " + response.getStatusInfo());
        LOGGER.info("==============================");
        return response;
    }

    /**
     * Ferme le client REST proprement
     */
    public void closeClient() {
        if (this.clientRest != null) {
            this.clientRest.close();
            LOGGER.info("Client REST fermé.");
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