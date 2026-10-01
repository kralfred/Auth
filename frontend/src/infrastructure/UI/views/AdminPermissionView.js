export class AdminPermissionView {
  constructor(adminService) {
    this.adminService = adminService;
    this.entityListContainer = null;
    this.attributeListContainer = null;
  }

  render() {
    const mainContainer = document.createElement("div");
    mainContainer.className = "admin-permissions-container";

    // Left Panel: Entities
    const leftPanel = this._createPanel("Entities", "Add Entity", () => {
      this._showCreateModal("Entity", async (entityName) => {
        await this.adminService.createEntityType(entityName);
        await this._loadEntities();
      });
    });

    this.entityListContainer = document.createElement("div");
    this.entityListContainer.className = "admin-panel-body";
    leftPanel.body.appendChild(this.entityListContainer);

    // Right Panel: Attributes
    const rightPanel = this._createPanel("Attributes", "Add Attribute", () => {
      const selectedEntity = this.adminService.getSelectedEntity();
      if (!selectedEntity) {
        alert("Please select an entity first!");
        return;
      }
      this._showCreateModal("Attribute", async (attrName) => {
        await this.adminService.createPermissionAttribute(attrName);
        await this._loadAttributes();
      });
    });

    this.attributeListContainer = document.createElement("div");
    this.attributeListContainer.className = "admin-panel-body";
    rightPanel.body.appendChild(this.attributeListContainer);

    this.attributeListContainer.innerHTML = `<p class="placeholder-text">Select an entity from the left to view its attributes.</p>`;

    mainContainer.appendChild(leftPanel.element);
    mainContainer.appendChild(rightPanel.element);

    // Initial load
    this._loadEntities();

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

  async _loadEntities() {
    this.entityListContainer.innerHTML = "<p>Loading entities...</p>";
    try {
      const entities = await this.adminService.loadEntities();
      this.entityListContainer.innerHTML = "";

      if (!entities || entities.length === 0) {
        this.entityListContainer.innerHTML = "<p>No entities found.</p>";
        return;
      }

      entities.forEach((entity) => {
        const item = document.createElement("div");
        item.className = "entity-item";
        item.textContent = entity.name || entity.id;

        const selected = this.adminService.getSelectedEntity();
        if (selected && selected.id === entity.id) {
          item.classList.add("selected");
        }

        item.onclick = async () => {
          this.adminService.setSelectedEntity(entity);

          // Update CSS selection styling
          Array.from(this.entityListContainer.children).forEach(child => child.classList.remove("selected"));
          item.classList.add("selected");

          await this._loadAttributes();
        };

        this.entityListContainer.appendChild(item);
      });
    } catch (e) {
      this.entityListContainer.innerHTML = `<p style="color: red;">Failed to load entities: ${e.message}</p>`;
    }
  }

  async _loadAttributes() {
    this.attributeListContainer.innerHTML = "<p>Loading attributes...</p>";

    try {
      const attributes = await this.adminService.loadSelectedEntityAttributes();
      this.attributeListContainer.innerHTML = "";

      if (!attributes || attributes.length === 0) {
        const selected = this.adminService.getSelectedEntity();
        const selectedName = selected ? (selected.name || selected.id) : "entity";
        this.attributeListContainer.innerHTML = `<p class="placeholder-text">No attributes found for ${selectedName}.</p>`;
        return;
      }

      attributes.forEach((attr) => {
        const item = document.createElement("div");
        item.className = "attribute-item";
        item.textContent = typeof attr === "string" ? attr : attr.name || attr.attributeName;
        this.attributeListContainer.appendChild(item);
      });
    } catch (e) {
      this.attributeListContainer.innerHTML = `<p style="color: red;">Failed to load attributes: ${e.message}</p>`;
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

    submitBtn.onclick = async () => {
      const val = input.value.trim();
      if (!val) return;

      submitBtn.disabled = true;
      submitBtn.textContent = "Saving...";

      try {
        await onSubmit(val);
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