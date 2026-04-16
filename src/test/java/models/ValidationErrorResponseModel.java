package models;

import java.util.List;

public record ValidationErrorResponseModel(List<String> username, List<String> password) {}
