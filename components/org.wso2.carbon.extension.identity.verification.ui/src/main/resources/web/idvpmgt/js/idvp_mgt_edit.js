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

const deleteClaimRow = (rowId) => {
    $(`#claim-row_${rowId}`).remove();
    handleClaimAddTableVisibility();
};

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

const handleClaimAddTableVisibility = () => {

    if ($('#claimAddTable tr').length >= 2) {
        $('#claimAddTable').show();
    } else {
        $('#claimAddTable').hide();
    }
}

/**
 * Renders the configuration property section according to the given metadata and current config properties.
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
 * @param property The property to be rendered.
 * @param currentConfigProperties The current configuration properties of the identity verification provider.
 * @returns Rendered password input field.
 */
const renderPasswordField = (property, currentConfigProperties)=> {
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
 * @param existingIdVProviderNames The names of the existing Identity Verification Providers.
 * @param currentIdVPName In the IdV Provider edit mode, the name of the current IdV provider. This is used to prevent
 *                        triggering an error in the name validation when the name is not changed.
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
        if (elementMetadata.required && isFieldEmpty(`#${element.id}`)) {
            CARBON.showWarningDialog(`${elementMetadata.label} cannot be empty`);
            return false;
        }

        // TODO: Handle default validations according to the input type.
        // switch (elementMetadata.type) {
        //     case INPUT_PASSWORD:
        //         console.log("password", element.value, element)
        //         break;
        // }

        // Perform regex validation if a regex is defined in the metadata.
        if (elementMetadata[METADATA_KEY_VALIDATION_REGEX] &&
            !element.value.match(elementMetadata[METADATA_KEY_VALIDATION_REGEX])) {
            console.log("inside");
            const errorMsg = elementMetadata[METADATA_KEY_REGEX_ERROR] ? elementMetadata[METADATA_KEY_REGEX_ERROR]
                : `Input value of the "${elementMetadata.label}" field does not match the expected format`;
            console.log("error", errorMsg);
            CARBON.showWarningDialog(errorMsg);
            return false;
        }
    }

    return true;
}

/**
 * Validates the claim configuration.
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
 * @param id The id of the field.
 * @returns True if the field is empty. False otherwise.
 */
const isFieldEmpty = (id) => {
    return $(id).val().trim() === "";
}
