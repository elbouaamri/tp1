package ma.emsi.elbouaamri.tp1_elbouaamri;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Named
@ViewScoped
public class Bb implements Serializable {

    private String roleSysteme;
    private boolean roleSystemeChangeable = true;
    private List<SelectItem> listeRolesSysteme;
    private String question;
    private String reponse;
    private StringBuilder conversation = new StringBuilder();

    private String texteRequeteJson;
    private String texteReponseJson;
    private boolean debug = true; // Pour activer l’affichage du JSON



    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }




    public String getT() {
        return texteReponseJson;
    }

    public void setTexteReponseJson(String texteReponseJson) {
        this.texteReponseJson = texteReponseJson;
    }

    @Inject
    private FacesContext facesContext;

    @Inject
    private JsonUtilPourGemini jsonUtil; // 🔹 Injection du composant pour envoyer la requête LLM

    public Bb() {}

    // --- Getters et Setters ---
    public String getRoleSysteme() { return roleSysteme; }
    public void setRoleSysteme(String roleSysteme) { this.roleSysteme = roleSysteme; }
    public boolean isRoleSystemeChangeable() { return roleSystemeChangeable; }
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public String getReponse() { return reponse; }
    public void setReponse(String reponse) { this.reponse = reponse; }
    public String getConversation() { return conversation.toString(); }
    public void setConversation(String conversation) { this.conversation = new StringBuilder(conversation); }
    public String getTexteRequeteJson() { return texteRequeteJson; }
    public String getTexteReponseJson() { return texteReponseJson; }

    public List<SelectItem> getRolesSysteme() {
        if (this.listeRolesSysteme == null) {
            this.listeRolesSysteme = new ArrayList<>();
            String role = """
                    You are a helpful assistant. You help the user to find the information they need.
                    If the user type a question, you answer it.
                    """;
            this.listeRolesSysteme.add(new SelectItem(role, "Assistant"));

            role = """
                    You are an interpreter. You translate from English to French and from French to English.
                    If the user type a French text, you translate it into English.
                    If the user type an English text, you translate it into French.
                    If the text contains only one to three words, give some examples of usage of these words in English.
                    """;
            this.listeRolesSysteme.add(new SelectItem(role, "Traducteur Anglais-Français"));

            role = """
                    You are a travel guide. If the user type the name of a country or of a town,
                    you tell them what are the main places to visit in the country or the town
                    and you tell them the average price of a meal.
                    """;
            this.listeRolesSysteme.add(new SelectItem(role, "Guide touristique"));
        }
        return this.listeRolesSysteme;
    }

    /**
     * Envoie la question à l’API Gemini via JsonUtilPourGemini.
     */
    public String envoyer() {
        if (question == null || question.isBlank()) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Texte question vide", "Il manque le texte de la question");
            facesContext.addMessage(null, message);
            return null;
        }

        try {
            // Envoi de la requête et récupération de la réponse finale
            String reponseTextuelle = jsonUtil.envoyerRequete(question);
            this.texteRequeteJson = jsonUtil.getTexteRequeteJson(); // JSON envoyé
            this.texteReponseJson = reponseTextuelle;                // réponse reçue
            this.reponse = reponseTextuelle;                         // texte de la réponse

        } catch (Exception e) {
            FacesMessage message =
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Problème de connexion avec l'API du LLM",
                            "Problème de connexion avec l'API du LLM : " + e.getMessage());
            facesContext.addMessage(null, message);
            return null;
        }

        // 🔹 Verrouiller le rôle après le premier envoi
        if (this.conversation.isEmpty()) {
            reponse = roleSysteme.toUpperCase(Locale.FRENCH) + "\n" + reponse;
            this.roleSystemeChangeable = false;
        }

        // 🔹 Ajouter à la conversation
        afficherConversation();
        return null;
    }


    /**
     * Récupère le libellé du rôle système pour une réponse conviviale.
     */
    private String getRoleLabel() {
        for (SelectItem item : getRolesSysteme()) {
            if (item.getValue().equals(roleSysteme)) {
                return item.getLabel();
            }
        }
        return "rôle personnalisé";
    }

    private void afficherConversation() {
        this.conversation.append("== User:\n")
                .append(question)
                .append("\n== Serveur:\n")
                .append(reponse)
                .append("\n");
    }

    public String nouveauChat() {
        return "index";
    }
}
