package tests.ui.pages;

import static com.codeborne.selenide.Selenide.$;
import static io.qameta.allure.Allure.step;

import com.codeborne.selenide.SelenideElement;

public class ReviewFormPage {

    private final SelenideElement assessmentInput = $("#assessment");
    private final SelenideElement readPagesInput = $("#readPages");
    private final SelenideElement reviewTextarea = $("#review");
    private final SelenideElement saveButton = $(".save-btn");
    private final SelenideElement cancelButton = $(".cancel-btn");

    public ReviewFormPage fill(int assessment, int readPages, String text) {
        return step("Заполнить форму отзыва: assessment=" + assessment + ", pages=" + readPages, () -> {
            assessmentInput.setValue(String.valueOf(assessment));
            readPagesInput.setValue(String.valueOf(readPages));
            reviewTextarea.setValue(text);
            return this;
        });
    }

    public void submit() {
        step("Опубликовать отзыв", () -> {
            saveButton.click();
        });
    }

    public void cancel() {
        step("Отменить написание отзыва", () -> {
            cancelButton.click();
        });
    }
}
