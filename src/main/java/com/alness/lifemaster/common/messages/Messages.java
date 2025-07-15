package com.alness.lifemaster.common.messages;

public class Messages {

    // error msj
    public static final String LOG_ERROR_DATA_INTEGRITY = "Data integrity error: [{}]";
    public static final String LOG_ERROR_TO_SAVE_ENTITY = "An unexpected error occurred while saving the registry: [{}]";
    public static final String LOG_ERROR_TO_UPDATE_ENTITY = "An unexpected error occurred while updating the registry: [{}]";
    public static final String LOG_ERROR_TO_DELETE_ENTITY = "An unexpected error occurred while deleting the registry: [{}]";
    public static final String LOG_ERROR_API = "Error in class: {}, method: {}. message: {}";
    public static final String TOKEN_ERROR = "Token de sesión no valido o expirado.";
    public static final String SESSION_ERROR = "Sesión no válida o expirada.";

    public static final String ERROR_ENTITY_SAVE = "Se produjo un error inesperado al guardar el registro.";
    public static final String ERROR_ENTITY_UPDATE = "Se produjo un error inesperado al actualizar el registro.";
    public static final String ERROR_ENTITY_DELETE = "Se produjo un error inesperado al eliminar el registro.";
    public static final String ERROR_FILE_DOWNLOAD = "Se produjo un error inesperado al descargar el archivo.";
    public static final String ENTITY_DELETE = "El recurso con id: [%s] fue eliminado";
    public static final String NOT_FOUND = "El recurso solicitado con id o nombre: [%s] no fue encontrado.";
    public static final String NOT_FOUND_BASIC = "El recurso solicitado no fue encontrado.";
    public static final String NOT_FOUND_FILE = "El archivo solicitado con id: [%s] no fue encontrado.";
    public static final String DATA_INTEGRITY = "Violación de la integridad de los datos.";
    public static final String FORBIDEN_UPDATE_DATA = "El usuario con id [%s] no tiene permiso para actualizar esta información.";

    private Messages() {
        throw new IllegalStateException("Utility class");
    }

}
