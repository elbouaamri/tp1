package ma.emsi.elbouaamri.tp1_elbouaamri;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
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

    @Inject
    private FacesContext facesContext;

    public Bb() {
    }

    // Getters et Setters
    public String getRoleSysteme() {
        return roleSysteme;
    }

    public void setRoleSysteme(String roleSysteme) {
        this.roleSysteme = roleSysteme;
    }

    public boolean isRoleSystemeChangeable() {
        return roleSystemeChangeable;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getReponse() {
        return reponse;
    }

    public void setReponse(String reponse) {
        this.reponse = reponse;
    }

    public String getConversation() {
        return conversation.toString();
    }

    public void setConversation(String conversation) {
        this.conversation = new StringBuilder(conversation);
    }

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
                    Your are a travel guide. If the user type the name of a country or of a town,
                    you tell them what are the main places to visit in the country or the town
                    and you tell them the average price of a meal.
                    """;
            this.listeRolesSysteme.add(new SelectItem(role, "Guide touristique"));
        }
        return this.listeRolesSysteme;
    }

    /**
     * Analyse le type de question (interrogative, explicative, déclarative) et génère une réponse
     * contextualisée avec le rôle système.
     * @return null pour rester sur la même page.
     */
    public String envoyer() {
        if (question == null || question.isBlank()) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Texte question vide", "Il manque le texte de la question");
            facesContext.addMessage(null, message);
            return null;
        }

        // Liste de mots-clés pour identifier les types de questions
        List<String> interrogativeKeywords = Arrays.asList("comment", "pourquoi", "quoi", "quand", "où", "qui", "est-ce");
        List<String> explicativeKeywords = Arrays.asList("explique", "décris", "défini", "analyse");

        // Analyse de la question
        String lowerCaseQuestion = question.toLowerCase(Locale.FRENCH).trim();
        String questionType;
        boolean isInterrogative = false;
        boolean isExplicative = false;

        // Vérifier les mots-clés
        for (String keyword : interrogativeKeywords) {
            if (lowerCaseQuestion.startsWith(keyword)) {
                isInterrogative = true;
                break;
            }
        }
        for (String keyword : explicativeKeywords) {
            if (lowerCaseQuestion.contains(keyword)) {
                isExplicative = true;
                break;
            }
        }

        // Déterminer le type de question
        if (isInterrogative) {
            questionType = "interrogative";
            reponse = "En tant que " + getRoleLabel() + ", je détecte une question interrogative : \"" + question + "\"";
        } else if (isExplicative) {
            questionType = "explicative";
            reponse = "En tant que " + getRoleLabel() + ", je détecte une question explicative : \"" + question + "\"";
        } else {
            questionType = "déclarative";
            reponse = "En tant que " + getRoleLabel() + ", je détecte une question déclarative : \"" + question + "\"";
        }

        // Ajouter le rôle système au début de la conversation si elle est vide
        if (this.conversation.isEmpty()) {
            reponse = roleSysteme.toUpperCase(Locale.FRENCH) + "\n" + reponse;
            this.roleSystemeChangeable = false;
        }

        // Ajouter à la conversation
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
        this.conversation.append("== User:\n").append(question).append("\n== Serveur:\n").append(reponse).append("\n");
    }

    public String nouveauChat() {
        return "index";
    }
}