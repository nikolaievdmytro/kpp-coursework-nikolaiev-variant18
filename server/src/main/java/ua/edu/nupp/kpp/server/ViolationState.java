package ua.edu.nupp.kpp.server;

/**
 * Стани життєвого циклу порушення:
 * ВИЯВЛЕНО → ПЕРЕВІРЕНО → ЗАХІД_ЗАПЛАНОВАНО → ЗАКРИТО.
 */
public enum ViolationState {
    DETECTED,
    VERIFIED,
    ACTION_PLANNED,
    CLOSED
}
