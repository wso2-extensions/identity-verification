const deleteClaimRows = [];
const deleteClaimRow = (rowId) => {
    console.log("Deleting claim row: " + rowId);
    $(`#claim-row_${rowId}`).remove();
    handleClaimAddTableVisibility();
};

const generateHTMLForClaimMappingRows = (claimRowId, option) => {

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
          <select class="claimrow_wso2" name="claimrow_name_wso2_${claimRowId}">
            ${option}
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
