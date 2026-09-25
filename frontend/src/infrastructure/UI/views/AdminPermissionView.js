


export class AdminPermissionView {
  constructor(adminService, entityService) {
    this.adminService = adminService;
    this.entityService = entityService;
    this.selectedEntity = null;
    this.entities = [];
  }

  render() {
    const mainContainer = document.createElement("div");
    mainContainer.className = "admin-permissions-container";

    // Left Panel: Entity List
    const leftPanel = this._createPanel("Entities", "Add Entity", () => {
      this._showCreateModal("Entity", async (entityName) => {
        await this.adminService.createEntityType(entityName);
        await this._loadEntities(entityListContainer);
      });
    });

    const entityListContainer = document.createElement("div");
    entityListContainer.className = "admin-panel-body";
    leftPanel.body.appendChild(entityListContainer);

    // Right Panel: Attribute List
    const rightPanel = this._createPanel("Attributes", "Add Attribute", () => {
      if (!this.selectedEntity) {
        alert("Please select an entity first!");
        return;
      }
      this._showCreateModal("Attribute", async (attrName) => {
        await this.entityService.createPermissionAttribute(
          attrName,
          this.selectedEntity.name || this.selectedEntity
        );
        await this._loadAttributes(attributeListContainer, this.selectedEntity);
      });
    });

    const attributeListContainer = document.createElement("div");
    attributeListContainer.className = "admin-panel-body";
    rightPanel.body.appendChild(attributeListContainer);

    attributeListContainer.innerHTML = `<p class="placeholder-text">Select an entity from the left to view its attributes.</p>`;

    mainContainer.appendChild(leftPanel.element);
    mainContainer.appendChild(rightPanel.element);

    this._loadEntities(entityListContainer, attributeListContainer);

    return mainContainer;
  }

  _createPanel(titleText, buttonText, onButtonClick) {
    const panel = document.createElement("div");
    panel.className = "admin-panel";

    const header = document.createElement("h3");
    header.className = "admin-panel-header";
    header.textContent = titleText;

    const body = document.createElement("div");

    const footer = document.createElement("div");
    footer.className = "admin-panel-footer";

    const btn = document.createElement("button");
    btn.className = "admin-panel-btn";
    btn.textContent = `+ ${buttonText}`;
    btn.onclick = onButtonClick;

    footer.appendChild(btn);
    panel.append(header, body, footer);

    return { element: panel, body };
  }

  async _loadEntities(container, attributeListContainer) {
    container.innerHTML = "<p>Loading entities...</p>";
    try {
      this.entities = (await this.adminService.loadEntities()) || [];
      container.innerHTML = "";

      if (this.entities.length === 0) {
        container.innerHTML = "<p>No entities found.</p>";
        return;
      }

      this.entities.forEach((entity) => {
        const item = document.createElement("div");
        item.className = "entity-item";
        const entityName = typeof entity === "string" ? entity : entity.name || entity.id;
        item.textContent = entityName;

        item.onclick = () => {
          this.selectedEntity = entity;
          Array.from(container.children).forEach(child => child.classList.remove("selected"));
          item.classList.add("selected");

          if (attributeListContainer) {
            this._loadAttributes(attributeListContainer, entity);
          }
        };

        container.appendChild(item);
      });
    } catch (e) {
      container.innerHTML = `<p style="color: red;">Failed to load entities: ${e.message}</p>`;
    }
  }

  async _loadAttributes(container, entity) {
    container.innerHTML = "<p>Loading attributes...</p>";
    try {
      const entityName = typeof entity === "string" ? entity : entity.name || entity.id;
      const attributes = await this.entityService.getEntityById("attributes", entityName);
      
      container.innerHTML = "";
      const attrList = Array.isArray(attributes) ? attributes : attributes.attributes || [];

      if (attrList.length === 0) {
        container.innerHTML = `<p class="placeholder-text">No attributes found for ${entityName}.</p>`;
        return;
      }

      attrList.forEach((attr) => {
        const item = document.createElement("div");
        item.className = "attribute-item";
        item.textContent = typeof attr === "string" ? attr : attr.name || attr.attributeName;
        container.appendChild(item);
      });
    } catch (e) {
      container.innerHTML = `<p style="color: red;">Failed to load attributes: ${e.message}</p>`;
    }
  }

  _showCreateModal(typeLabel, onSubmit) {
  const overlay = document.createElement("div");
  overlay.className = "modal-overlay";

  const modal = document.createElement("div");
  modal.className = "modal-content";

  const title = document.createElement("h3");
  title.textContent = `Create New ${typeLabel}`;

  const input = document.createElement("input");
  input.type = "text";
  input.className = "modal-input";
  input.placeholder = `${typeLabel} Name`;

  const actions = document.createElement("div");
  actions.className = "modal-actions";

  const cancelBtn = document.createElement("button");
  cancelBtn.className = "btn-cancel";
  cancelBtn.textContent = "Cancel";
  cancelBtn.onclick = () => document.body.removeChild(overlay);

  const submitBtn = document.createElement("button");
  submitBtn.className = "btn-submit";
  submitBtn.textContent = "Create";

  // Handle click on submit button
  submitBtn.onclick = async () => {
    const val = input.value.trim();
    if (!val) return;

    submitBtn.disabled = true;
    submitBtn.textContent = "Saving...";

    try {
      // Execute the callback function passed from render()
      await onSubmit(val);
      
      // Close modal on success
      document.body.removeChild(overlay);
    } catch (err) {
      alert(`Error creating ${typeLabel}: ${err.message}`);
      submitBtn.disabled = false;
      submitBtn.textContent = "Create";
    }
  };

  actions.append(cancelBtn, submitBtn);
  modal.append(title, input, actions);
  overlay.appendChild(modal);
  document.body.appendChild(overlay);

  input.focus();
}
}