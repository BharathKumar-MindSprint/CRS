package com.olam.digital.crs.model

import com.olam.digital.crs.extension.normalizeToken
import com.olam.digital.crs.room.CRSResponseTypeDTO
import timber.log.Timber

enum class QuestionType {
    RADIO,
    CHECKBOX,
    EDITTEXT,      // Textbox
    PHOTO,         // Photo upload
    RATING,
    DROPDOWN,
    DATEPICKER,
    LOCATION,
    YESNO,
    PHOTO_CHECKBOX,
    FILE_UPLOAD,

    SIGNATURE
}

fun CRSResponseTypeDTO.toQuestionType(): QuestionType? = when (name?.normalizeToken()) {
    "radio" -> QuestionType.RADIO
    "checkbox" -> QuestionType.CHECKBOX
    "textbox", "text", "edittext"
        -> QuestionType.EDITTEXT

    "photoupload", "photo", "image", "imageupload"
        -> QuestionType.PHOTO              // handles "Photo upload"
    "rating" -> QuestionType.RATING
    "dropdown", "select"
        -> QuestionType.DROPDOWN

    "datepicker", "date"
        -> QuestionType.DATEPICKER         // handles "Date Picker"
    "location", "gps", "geolocation"
        -> QuestionType.LOCATION

    "yesno", "boolean" -> QuestionType.YESNO
    "photocheckbox", "imagecheckbox"
        -> QuestionType.PHOTO_CHECKBOX     // handles "Photo Checkbox"
    "fileupload", "documentupload", "file"
        -> QuestionType.FILE_UPLOAD        // handles "File upload"
    "signature" -> QuestionType.SIGNATURE
    else -> null
}


// -----------------------------------------
// VIEW TYPES
// -----------------------------------------
enum class ViewType {
    CHECKBOX,
    DATE_PICKER,
    DROPDOWN,
    LOCATION,
    PHOTO_CHECKBOX,
    PHOTO_UPLOAD,
    RADIO,
    TEXTBOX,
    FILE_UPLOAD,
    SIGNATURE
}
