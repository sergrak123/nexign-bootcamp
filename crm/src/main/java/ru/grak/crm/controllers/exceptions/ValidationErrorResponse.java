package ru.grak.crm.controllers.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * Ответ на запрос с сообщениями об ошибках валидации.
 */
@Getter
@RequiredArgsConstructor
public class ValidationErrorResponse {

    private final List<Violation> violations;

}

/**
 * Класс представляющий нарушение валидации.
 */
@Getter
@RequiredArgsConstructor
class Violation {

    private final String field;
    private final String message;

}


