/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

// Input Types
const INPUT_DEFAULT = "default";
const INPUT_IDENTIFIER = "identifier";
const INPUT_NAME = "name";
const INPUT_NUMBER = "number";
const INPUT_RESOURCE_NAME = "resource_name";
const INPUT_CLIENT_ID = "client_id";
const INPUT_DESCRIPTION = "description";
const INPUT_EMAIL = "email";
const INPUT_URL = "url";
const INPUT_PASSWORD = "password";
const INPUT_CHECKBOX = "checkbox";
const INPUT_TOGGLE = "toggle";
const INPUT_TEXT_AREA = "text_area";
const INPUT_DROPDOWN = "dropdown";

// Other constants
const CHECKED = "checked";
const METADATA_KEY_VALIDATION_REGEX = "validationRegex";
const METADATA_KEY_REGEX_ERROR = "regexValidationError";

/**
 * Handles the claim mapping row deletion.
 *
 * @param rowId The ID of the row to be deleted.
 */
const deleteClaimRow = (rowId) => {
    $(`#claim-row_${rowId}`).remove();
    handleClaimAddTableVisibility();
};

/**
 * Generate a claim mapping row.
 *
 * @param options The options that needs to be added to the local claims dropdown
 * @returns Generated claim mapping row.
 */
const generateHTMLForClaimMappingRows = (options) => {
    const claimRowId = $("#claimAddTable tbody tr").length;
    return `
      <tr id="claim-row_${claimRowId}">
        <td>
          <input 
            class="external-claim" 
            style="width: 90%;" 
            type="text" 
            id="external-claim-id_${claimRowId}"
            name="external-claim-name_${claimRowId}"/>
        </td>
        <td>
          <select
            id="claim-row-id-wso2_${claimRowId}" 
            class="claim-row-wso2" 
            name="claim-row-name-wso2_${claimRowId}">
            ${options}
          </select>
        </td>
        <td>
          <a onclick="deleteClaimRow(${claimRowId})" class="icon-link delete-link">
            Delete
          </a>
        </td>
      </tr>
    `;
}

/**
 * Handles the visibility of the claim mapping table based on the number of rows.
 */
const handleClaimAddTableVisibility = () => {

    if ($('#claimAddTable tr').length >= 2) {
        $('#claimAddTable').show();
    } else {
        $('#claimAddTable').hide();
    }
}

/**
 * Renders the configuration property section according to the given metadata and current config properties.
 *
 * @param metadata The UI metadata of the identity verification provider.
 * @param currentConfigProperties The current configuration properties of the identity verification provider. Values of
 * this map will override the default values in the metadata.
 */
const renderConfigurationPropertySection = (metadata, currentConfigProperties) => {

    const configPropertyTable = $("#config-property-table");
    // Clear the previous configuration properties before rendering new ones
    configPropertyTable.empty();

    // Handle the case where there are no configuration properties
    if (!metadata || !metadata["common"] || !metadata["common"]["configProperties"]) {
        CARBON.showErrorDialog("No configuration properties found for the identity verification provider.");
        return;
    }

    // Render the configuration properties
    const properties = metadata["common"]["configProperties"];
    const TYPE_TEXT = "text";
    for (const property of properties) {
        let element;
        switch (property.type) {
            case INPUT_PASSWORD:
                element = renderPasswordField(property, currentConfigProperties);
                break;
            case INPUT_DESCRIPTION:
            case INPUT_CLIENT_ID:
            case INPUT_RESOURCE_NAME:
            case INPUT_NAME:
            case INPUT_IDENTIFIER:
            case INPUT_DEFAULT:
                element = renderInputField(TYPE_TEXT, property, currentConfigProperties);
                break;
            case INPUT_URL:
            case INPUT_EMAIL:
            case INPUT_NUMBER:
                element = renderInputField(property.type, property, currentConfigProperties);
                break;
            case INPUT_CHECKBOX:
            case INPUT_TOGGLE:
                element = renderCheckBoxField(property, currentConfigProperties);
                break;
            case INPUT_TEXT_AREA:
                element = renderTextAreaField(property, currentConfigProperties);
                break;
            case INPUT_DROPDOWN:
                element = renderDropdownField(property);
                break;
            default:
                element = renderInputField(TYPE_TEXT, property, currentConfigProperties);
        }
        configPropertyTable.append(element);
    }
}

/**
 * Renders the label element for a given label and required status.
 *
 * @param label The text to be displayed as the label.
 * @param required Whether the field is required or not.
 * @returns Rendered label element.
 */
const renderLabelElement = (label, required) => {
    return `
        <td class="leftCol-med labelField">
            ${label}:${required ? "<span class='required'>*</span>" : ""}
        </td>
    `;
}

/**
 * Handles the visibility of the text inside a password field.
 *
 * @param toggleButtonId Id of the toggle button.
 * @param passwordFieldId Id of the password field.
 */
const showHidePassword = (toggleButtonId, passwordFieldId) => {
    const passwordElement = $(`#${passwordFieldId}`);
    const toggleButton = $(`#${toggleButtonId}`);

    if (toggleButton.hasClass("hideMode")) {
        passwordElement.attr("type", "text");
        toggleButton.text("Hide");
        toggleButton.removeClass("hideMode");
    } else {
        passwordElement.attr("type", "password");
        toggleButton.text("Show");
        toggleButton.addClass("hideMode");
    }
}

/**
 * A utility method used to resolve the default value of a given property.
 *
 * @param property The property of which the default values need to be resolved.
 * @param currentConfigProperties The current configuration properties of the identity verification provider.
 * @returns The resolved default value.
 */
const getDefaultValue = (property, currentConfigProperties) => {
    if (property && currentConfigProperties && currentConfigProperties.get(property.name)) {
        if (property.type === INPUT_CHECKBOX || property.type === INPUT_TOGGLE) {
            return toBoolean(currentConfigProperties.get(property.name)) ? CHECKED : "";
        }
        return currentConfigProperties.get(property.name);
    } else if (property && property.defaultValue) {
        if (property.type === INPUT_CHECKBOX || property.type === INPUT_TOGGLE) {
            return toBoolean(property.defaultValue) ? CHECKED : "";
        }
        return property.defaultValue;
    }
    return "";
}

/**
 * Utility function to convert a value to a boolean.
 *
 * @param value The value to be converted.
 * @returns Converted boolean value.
 */
const toBoolean = (value) => {
    if (typeof value === "boolean") {
        return value;
    }
    return value === "true";
}

/**
 * Renders a password input field for the given property.
 *
 * @param property The property to be rendered.
 * @param currentConfigProperties The current configuration properties of the identity verification provider.
 * @returns Rendered password input field.
 */
const renderPasswordField = (property, currentConfigProperties) => {
    return `
        <tr>
            ${renderLabelElement(property.label, property.required)}
            <td>
                <div class="passwordFieldContainer">
                    <input
                      type="password"
                      id="${property.name}"
                      name="${property.name}"
                      class="configPropertyField"
                      placeholder="${property.placeholder ? property.placeholder : ''}"
                      minlength="${property.minLength ? property.minLength : ''}"
                      maxlength="${property.maxLength ? property.maxLength : ''}"
                      value="${getDefaultValue(property, currentConfigProperties)}"/>
                    <span>
                        <a id="toggleId_${property.name}"
                           class="showHideBtn hideMode"
                           onclick="showHidePassword('toggleId_${property.name}', '${property.name}')">
                           Show
                        </a>
                    </span>
                </div>
                <div class="sectionHelp">
                    ${property.hint}
                </div>
            </td>
        </tr>
    `;
}

/**
 * Renders an input field for the given property.
 *
 * @param type The type of the input field.
 * @param property The property to be rendered.
 * @param currentConfigProperties The current configuration properties as a map.
 * @returns Rendered input field.
 */
const renderInputField = (type, property, currentConfigProperties) => {
    return `
        <tr>
            ${renderLabelElement(property.label, property.required)}
            <td>
                <input
                  type="${type}"
                  id="${property.name}"
                  name="${property.name}"
                  class="configPropertyField"
                  placeholder="${property.placeholder ? property.placeholder : ''}"
                  minlength="${property.minLength ? property.minLength : ''}"
                  maxlength="${property.maxLength ? property.maxLength : ''}"
                  value="${getDefaultValue(property, currentConfigProperties)}"/>
                <div class="sectionHelp">
                    ${property.hint}
                </div>
            </td>
        </tr>
    `;
}

/**
 * Renders a dropdown field for the given property.
 *
 * @param property The property to be rendered.
 * @param currentConfigProperties The current configuration properties as a map.
 * @returns Rendered dropdown field.
 */
const renderCheckBoxField = (property, currentConfigProperties) => {
    return `
        <tr>
            ${renderLabelElement(property.label, property.required)}
            <td>
                <div class="sectionCheckbox">
                    <input
                      type="checkbox"
                      id="${property.name}"
                      name="${property.name}"
                      class="configPropertyField"
                      value="true"
                      ${getDefaultValue(property, currentConfigProperties)}/>
                    <div class="sectionHelp">
                        ${property.hint}
                    </div>
                </div>  
            </td>
        </tr>
    `;
}

/**
 * Renders a text area field for the given property.
 *
 * @param property The property to be rendered.
 * @param currentConfigProperties The current configuration properties as a map.
 * @returns Rendered text area field.
 */
const renderTextAreaField = (property, currentConfigProperties) => {
    return `
        <tr>
            ${renderLabelElement(property.label, property.required)}
            <td>
                <textarea
                  id="${property.name}"
                  name="${property.name}"
                  class="configPropertyField"
                  placeholder="${property.placeholder ? property.placeholder : ''}"
                  minlength="${property.minLength ? property.minLength : ''}"
                  maxlength="${property.maxLength ? property.maxLength : ''}"
                >${getDefaultValue(property, currentConfigProperties)}</textarea>
                <div class="sectionHelp">
                    ${property.hint}
                </div>
            </td>
        </tr>
    `;
}

/**
 * Renders a dropdown field for the given property.
 *
 * @param property The property to be rendered.
 * @param currentConfigProperties The current configuration properties as a map.
 * @returns Rendered dropdown field.
 */
const renderDropdownField = (property, currentConfigProperties) => {

    /**
     * Returns the options for the dropdown field.
     * @returns Options for the dropdown field.
     */
    const getDropdownOptions = () => {
        let options = "";
        for (const option of property.options) {
            if (currentConfigProperties && currentConfigProperties.get(property.name)) {
                if (currentConfigProperties.get(property.name) === option.value) {
                    options += `<option value="${option.value}" selected>${option.label}</option>`;
                    continue;
                }
            } else if (property.defaultValue && property.defaultValue.value === option.value) {
                // property.defaultValue is only considered selecting a default value only if there is no existing
                // property in the currentConfigProperties.
                options += `<option value="${option.value}" selected>${option.label}</option>`;
                continue;
            }
            options += `<option value="${option.value}">${option.label}</option>`;
        }
        return options;
    }

    return `
        <tr>
            ${renderLabelElement(property.label, property.required)}
            <td>
                <select
                  class="selectField configPropertyField"
                  id="${property.name}"
                  name="${property.name}">
                  ${getDropdownOptions()}
                </select>
                <div class="sectionHelp">
                    ${property.hint}
                </div>
            </td>
        </tr>
    `;
}

/**
 * Handles the cancel button click event by redirecting to IdVP list page.
 */
const handleIdVPMgtCancel = () => {
    location.href = "idvp-mgt-list.jsp"
};

/**
 * Performs the validations on the form.
 *
 * @param existingIdVProviderNames The names of the existing Identity Verification Providers.
 * @param currentIdVPName In the IdV Provider edit mode, the name of the current IdV provider. This is used to prevent
 *                        triggering an error in the name validation when the name is not changed.
 * @param idvProviderUIMetadata The UI metadata of the Identity Verification Provider.
 * @returns True if the form is valid. False otherwise.
 */
const performValidation = (existingIdVProviderNames, currentIdVPName, idvProviderUIMetadata) => {

    if (!isIdVPNameValid(existingIdVProviderNames, currentIdVPName)) {
        return false;
    }

    // Validate the type of the Identity Verification Provider.
    if (isFieldEmpty("#idvp-type-dropdown")) {
        CARBON.showWarningDialog("Identity Verification Provider type cannot be empty");
        return false;
    }

    if (!isConfigurationPropertiesValid(idvProviderUIMetadata)) {
        return false;
    }

    return isClaimConfigurationValid();

}

/**
 * Validates the name of the Identity Verification Provider.
 *
 * @param existingIdVProviderNames The names of all existing Identity Verification Providers.
 * @param currentIdVPName In the IdV Provider edit mode, the name of the current IdV provider. This is used to prevent
 *                        triggering an error in the name validation when the name is not changed.
 * @returns True if the name is valid. False otherwise.
 */
const isIdVPNameValid = (existingIdVProviderNames, currentIdVPName) => {

    const idVPNameFiledId = "#idVPName";
    if (isFieldEmpty(idVPNameFiledId)) {
        CARBON.showWarningDialog("Name of Identity Verification Provider cannot be empty");
        return false;
    } else if (existingIdVProviderNames.includes($(idVPNameFiledId).val()) && currentIdVPName !== $(idVPNameFiledId).val()) {
        CARBON.showWarningDialog("Identity Verification Provider with the same name already exists");
        return false;
    }

    return true;
}

/**
 * Validates the configuration properties of the Identity Verification Provider.
 *
 * @param metadata UI metadata of the Identity Verification Provider.
 * @returns True if the configuration properties are valid. False otherwise.
 */
const isConfigurationPropertiesValid = (metadata) => {

    const configPropertyTable = $("#config-property-table");
    // Handle the case where there are no configuration properties
    if (!metadata || !metadata["common"] || !metadata["common"]["configProperties"]) {
        CARBON.showErrorDialog("No configuration properties found for the identity verification provider.");
        return false;
    }

    const propertyMetadata = metadata["common"]["configProperties"];
    for (const element of configPropertyTable.find(".configPropertyField")) {
        const elementMetadata = propertyMetadata.find(prop => prop.name === element.name);

        // Perform required validation.
        if (elementMetadata.required && isFieldEmpty(`#${element.id}`)) {
            CARBON.showWarningDialog(`${elementMetadata.label} cannot be empty`);
            return false;
        }

        // Perform validations based on input type.
        let isValid;
        switch (elementMetadata.type) {
            case INPUT_URL:
                isValid = validateURL(element.value, elementMetadata.label);
                break;
            case INPUT_EMAIL:
                isValid = validateEmail(element.value, elementMetadata.label);
                break;
            case INPUT_IDENTIFIER:
                isValid = validateIdentifier(element.value, elementMetadata.label);
                break;
            case INPUT_RESOURCE_NAME:
                isValid = validateResourceName(element.value, elementMetadata.label);
                break;
            case INPUT_CLIENT_ID:
                isValid = validateClientId(element.value, elementMetadata.label);
                break;
            case INPUT_DESCRIPTION:
                isValid = validateDescription(element.value, elementMetadata.label);
                break;
            case INPUT_NUMBER:
                isValid = validateNumber(element.value, elementMetadata);
                break;
            default:
                // skip this validation if the input type is not defined.
                isValid = true;

        }
        if (!isValid) {
            return false;
        }

        // Perform regex validation if a regex is defined in the metadata.
        if (elementMetadata[METADATA_KEY_VALIDATION_REGEX] &&
            !element.value.match(elementMetadata[METADATA_KEY_VALIDATION_REGEX])) {
            const errorMsg = elementMetadata[METADATA_KEY_REGEX_ERROR] ? elementMetadata[METADATA_KEY_REGEX_ERROR]
                : `Input value of the "${elementMetadata.label}" field does not match the expected format`;
            CARBON.showWarningDialog(errorMsg);
            return false;
        }
    }

    return true;
}

/**
 * Validates the claim configuration.
 *
 * @returns True if the claim configuration is valid. False otherwise.
 */
const isClaimConfigurationValid = () => {

    for (let i = 0; i <= $("#claimAddTable tbody tr").length - 1; i++) {
        if (isFieldEmpty(`#external-claim-id_${i}`)) {
            CARBON.showWarningDialog("External claims cannot be empty");
            return false;
        }

        if (isFieldEmpty(`#claim-row-id-wso2_${i}`)) {
            CARBON.showWarningDialog("Local claim URIs cannot be empty");
            return false;
        }
    }
    return true;
}

/**
 * Validate whether the given field is empty.
 *
 * @param id The id of the field.
 * @returns True if the field is empty. False otherwise.
 */
const isFieldEmpty = (id) => {

    return $(id).val().trim() === "";
}

/**
 * Validates whether the given value is a valid URL.
 *
 * Refer to {@link https://github.com/wso2/identity-apps/blob/master/modules/validation/src/validation.ts}
 * more information.
 *
 * @param value Input to be validated.
 * @param label Label of the input field.
 * @returns True if the value is a valid URL. False otherwise.
 */
const validateURL = (value, label) => {

    if (value && joi.string().uri().validate(value).error) {
        CARBON.showWarningDialog(`The URL in ${label} is not valid`);
        return false;
    }
    return true;
};

/**
 * Validates whether the given value is a valid email address.
 *
 * Refer to {@link https://github.com/wso2/identity-apps/blob/master/modules/validation/src/validation.ts}
 * more information.
 *
 * @param value Input to be validated.
 * @param label Label of the input field.
 * @returns True if the value is a valid email address. False otherwise.
 */
const validateEmail = (value, label) => {

    if (value && joi.string().email({tlds: false}).validate(value).error) {
        CARBON.showWarningDialog(`The email address in ${label} is not valid`);
        return false;
    }
    return true;
};

/**
 * Validates whether the given value is a valid client ID.
 *
 * Refer to {@link https://github.com/wso2/identity-apps/blob/master/modules/validation/src/validation.ts}
 * more information.
 *
 * @param value Input to be validated.
 * @param label Label of the input field.
 * @returns True if the value is a valid identifier. False otherwise.
 */
const validateIdentifier = (value, label) => {

    if (value && joi.string().alphanum().min(3).validate(value).error) {
        CARBON.showWarningDialog(`The identifier in ${label} is not valid`);
        return false;
    }
    return true;
};

/**
 * Validates whether the given value is a valid client ID.
 *
 * Refer to {@link https://github.com/wso2/identity-apps/blob/master/modules/validation/src/validation.ts}
 * more information.
 *
 * @param value Input to be validated.
 * @param label Label of the input field.
 * @returns True if the value is a valid resource name. False otherwise.
 */
const validateResourceName = (value, label) => {

    try {
        const result = joi.string().regex(new RegExp("^[a-zA-Z][a-zA-Z0-9-_. ]+$")).validate(value);
        if (value && result.error) {
            CARBON.showWarningDialog(`The resource name in ${label} is not valid`);
            return false;
        }
        return true;
    } catch (error) {
        return false;
    }
};


/**
 * The specification [1] does not have an exact format for client IDs.
 * They can be different from provider to provider. However,
 * we have enforced this validation a bit by saying , a client id
 * must not have line breaks or spaces.
 *
 * `Disallowed characters ∊ {\r\n\t\f\v\u00a0\u1680\u2000-\u200a\u2028\u2029\u202f\u205f\u3000\ufeff}`
 *
 * [1] {@link https://datatracker.ietf.org/doc/html/rfc6749#section-2.2}
 *
 * Refer to {@link https://github.com/wso2/identity-apps/blob/master/modules/validation/src/validation.ts}
 * more information.
 *
 * @param value  Input to be validated.
 * @param label Label of the input field.
 * @returns True if the client id is valid. False otherwise.
 */
const validateClientId = (value, label) => {
    try {
        const result = joi.string().regex(new RegExp("^[^\\s]*$")).validate(value);
        if (value && result.error) {
            CARBON.showWarningDialog(`The client id in ${label} is not valid`);
            return false;
        }
        return true;
    } catch (e) {
        return false;
    }
};

/**
 * This function validates long or short descriptions.This is useful
 * for text areas and other generic input description fields.
 *
 * Refer to {@link https://github.com/wso2/identity-apps/blob/master/modules/validation/src/validation.ts}
 * more information.
 *
 * @param value Input to be validated.
 * @param label Label of the input field.
 * @returns True if the description is valid. False otherwise.
 */
const validateDescription = (value, label) => {
    try {
        const result = joi.string().min(3).max(1024).validate(value);
        if (value && result.error) {
            CARBON.showWarningDialog(`The description in ${label} is not valid`);
            return false;
        }
        return true;
    } catch (e) {
        return false;
    }
};

/**
 * This function checks whether the given value is a valid number.
 *
 * @param value Input to be validated.
 * @param elementMetadata Metadata of the input field.
 * @returns True if the value is a valid number. False otherwise.
 */
const validateNumber = (value, elementMetadata) => {

    // If the value is empty, do not validate.
    if (!value) {
        return true;
    }

    if (joi.number().validate(value).error) {
        CARBON.showWarningDialog(`The value in ${elementMetadata.label} is not a number`);
        return false;
    }

    if (elementMetadata.minValue && elementMetadata.maxValue &&
        (value < elementMetadata.minValue || value > elementMetadata.maxValue)) {

        CARBON.showWarningDialog(`The number in ${elementMetadata.label} should be between 
            ${elementMetadata.minValue} and ${elementMetadata.maxValue}.`);
        return false;
    }
    return true;
}
