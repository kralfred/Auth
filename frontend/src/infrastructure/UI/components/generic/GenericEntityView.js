// infrastructure/UI/views/GenericEntityView.js
import { GenericDataView } from "./GenericDataView.js";

export class GenericEntityView {
  constructor(entityService, entityType, title) {
    this.entityService = entityService;
    this.entityType = entityType;
    this.title = title;
  }

  render() {
    const container = document.createElement("div");
    container.style.padding = "20px";

    const loadingIndicator = document.createElement("p");
    loadingIndicator.textContent = `Loading ${this.entityType}...`;
    container.appendChild(loadingIndicator);

    // Fetch data via EntityService
    this.entityService.getEntityList(this.entityType)
      .then(data => {
        container.innerHTML = ""; // Clear loading state

        const dataView = new GenericDataView({
          title: this.title,
          items: data,
          onItemClick: (item) => this.handleItemClick(item, container)
        });

        container.appendChild(dataView.render());
      })
      .catch(err => {
        container.innerHTML = "";
        const errorMsg = document.createElement("p");
        errorMsg.style.color = "red";
        errorMsg.textContent = `Failed to load ${this.entityType}: ${err.message}`;
        container.appendChild(errorMsg);
      });

    return container;
  }

  async handleItemClick(item, container) {
    if (!item.id) return;

    try {
      // Fetch single detailed object via EntityService
      const detailedItem = await this.entityService.getEntityById(this.entityType, item.id);
      
      container.innerHTML = "";
      const dataView = new GenericDataView({
        title: `${this.title} - Item #${item.id}`,
        detailItem: detailedItem
      });
      container.appendChild(dataView.render());
    } catch (e) {
      console.error("Could not fetch detail view:", e);
    }
  }
}