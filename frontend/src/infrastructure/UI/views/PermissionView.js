// infrastructure/UI/views/PermissionView.js
export class PermissionView {
  constructor(entityService) {
    this.entityService = entityService;
  }

  render() {
    const container = document.createElement("div");
    container.style.padding = "20px";
    container.style.maxWidth = "500px";

    const title = document.createElement("h2");
    title.textContent = "Create Permission Attribute";

    const form = document.createElement("form");
    form.style.display = "flex";
    form.style.flexDirection = "column";
    form.style.gap = "15px";

    // Attribute Name Input
    const attrNameGroup = document.createElement("div");
    const attrNameLabel = document.createElement("label");
    attrNameLabel.textContent = "Attribute Name:";
    attrNameLabel.style.display = "block";
    
    const attrNameInput = document.createElement("input");
    attrNameInput.type = "text";
    attrNameInput.required = true;
    attrNameInput.placeholder = "e.g., READ_PRIVILEGE";
    attrNameInput.style.width = "100%";
    attrNameInput.style.padding = "8px";

    attrNameGroup.append(attrNameLabel, attrNameInput);

    // Entity Type Name Input
    const entityTypeGroup = document.createElement("div");
    const entityTypeLabel = document.createElement("label");
    entityTypeLabel.textContent = "Entity Type Name:";
    entityTypeLabel.style.display = "block";

    const entityTypeInput = document.createElement("input");
    entityTypeInput.type = "text";
    entityTypeInput.required = true;
    entityTypeInput.placeholder = "e.g., DOCUMENT";
    entityTypeInput.style.width = "100%";
    entityTypeInput.style.padding = "8px";

    entityTypeGroup.append(entityTypeLabel, entityTypeInput);

    // Submit Button
    const submitBtn = document.createElement("button");
    submitBtn.type = "submit";
    submitBtn.textContent = "Create Attribute";
    submitBtn.style.padding = "10px";
    submitBtn.style.backgroundColor = "#27ae60";
    submitBtn.style.color = "white";
    submitBtn.style.border = "none";
    submitBtn.style.cursor = "pointer";

    // Status Message Box
    const statusMessage = document.createElement("div");
    statusMessage.style.marginTop = "10px";

    form.append(attrNameGroup, entityTypeGroup, submitBtn);
    container.append(title, form, statusMessage);

    // Form Submission Handler
    form.addEventListener("submit", async (e) => {
      e.preventDefault();
      statusMessage.textContent = "Creating attribute...";
      statusMessage.style.color = "#333";

      try {
        await this.entityService.createPermissionAttribute(
          attrNameInput.value.trim(),
          entityTypeInput.value.trim()
        );

        statusMessage.textContent = "Permission attribute created successfully!";
        statusMessage.style.color = "green";
        form.reset();
      } catch (error) {
        console.error("Failed to create attribute:", error);
        statusMessage.textContent = `Error: ${error.message}`;
        statusMessage.style.color = "red";
      }
    });

    return container;
  }
}