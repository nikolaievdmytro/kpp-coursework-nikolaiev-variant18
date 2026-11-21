package ua.edu.nupp.kpp.client;

/**
 * Стани порушення. Значення мають збігатися з серверним enum, бо стан передається через HTTP/JSON.
 * Допустимість переходів клієнт не перевіряє — це робить ViolationService на сервері.
 */
public enum ViolationState {
    DETECTED,
    VERIFIED,
    ACTION_PLANNED,
    CLOSED
}
