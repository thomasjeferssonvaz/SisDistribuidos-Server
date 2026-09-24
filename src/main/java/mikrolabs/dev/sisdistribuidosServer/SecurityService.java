package mikrolabs.dev.sisdistribuidosServer;

import org.passay.*;
import org.passay.data.CharacterData;
import org.passay.rule.CharacterRule;
import org.passay.rule.LengthRule;
import org.passay.rule.Rule;
import org.passay.rule.WhitespaceRule;

import java.util.Arrays;
import java.util.List;

public class SecurityService {

    private final PasswordValidator validator;

    public SecurityService() {
        // 1. Definindo o conjunto customizado de caracteres especiais permitidos
        CharacterData customSpecialChars = new CharacterData() {
            @Override
            public String getErrorCode() {
                return "INSUFFICIENT_SPECIAL";
            }

            @Override
            public String getCharacters() {
                // Símbolos informados: #, ., *, &, %, $, @, !, (, ), -, _, =, +, .
                return "#.*&%$@!()-_=+.";
            }
        };

        // 2. Configurando as regras baseadas na sua política
        List<Rule> rules = Arrays.asList(
                // Comprimento mínimo de 8 e máximo de 20 caracteres
                new LengthRule(8, 20),

                // Pelo menos 1 letra maiúscula
                new CharacterRule(org.passay.data.EnglishCharacterData.UpperCase, 1),

                // Pelo menos 1 letra minúscula
                new CharacterRule(org.passay.data.EnglishCharacterData.LowerCase, 1),

                // Pelo menos 1 número (dígito)
                new CharacterRule(org.passay.data.EnglishCharacterData.Digit, 1),

                // Pelo menos 1 caractere especial restrito à nossa lista
                new CharacterRule(customSpecialChars, 1),

                // Boa prática: proibir espaços em branco no meio da senha
                new WhitespaceRule()
        );

        // No Passay 2.0.0, usamos DefaultPasswordValidator que implementa a interface PasswordValidator
        this.validator = new DefaultPasswordValidator(rules);
    }

    public ValidationResultDTO validate(String rawPassword) {
        PasswordData passwordData = new PasswordData(rawPassword);

        // 1. O retorno do validate no Passay 2.0.0 é ValidationResult
        ValidationResult result = validator.validate(passwordData);

        if (result.isValid()) {
            return new ValidationResultDTO(true, "Senha válida.");
        } else {
            // 2. No Passay 2.0.0, pegamos as mensagens diretamente do objeto de resultado ou formatadas
            List<String> messages = result.getMessages();
            return new ValidationResultDTO(false, String.join(" | ", messages));
        }
    }

    // Classe auxiliar de retorno
    public static class ValidationResultDTO {
        private final boolean valid;
        private final String message;

        public ValidationResultDTO(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
    }
}