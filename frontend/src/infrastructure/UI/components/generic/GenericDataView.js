// infrastructure/UI/views/GenericDataView.js
export class GenericDataView {
  /**
   * @param {Object} options
   * @param {string} options.title - Page heading (e.g., "User Directory", "System Logs")
   * @param {Array<Object>} [options.items] - List of objects to display
   * @param {Object} [options.detailItem] - Single object to display in detail
   * @param {Function} [options.onItemClick] - Optional click handler when a list row is selected
   */
  constructor({ title, items = null, detailItem = null, onItemClick = null }) {
    this.title = title;
    this.items = items;
    this.detailItem = detailItem;
    this.onItemClick = onItemClick;
  }

  render() {
    const container = document.createElement("div");
    container.style.padding = "20px";

    const header = document.createElement("h2");
    header.textContent = this.title;
    container.appendChild(header);

    if (this.detailItem) {
      container.appendChild(this._renderDetailView(this.detailItem));
    } else if (Array.isArray(this.items) && this.items.length > 0) {
      container.appendChild(this._renderListView(this.items));
    } else {
      const emptyMsg = document.createElement("p");
      emptyMsg.textContent = "No data available.";
      container.appendChild(emptyMsg);
    }

    return container;
  }

  // Renders a list/table automatically using the object's keys as headers
  _renderListView(items) {
    const table = document.createElement("table");
    table.style.width = "100%";
    table.style.borderCollapse = "collapse";

    // Extract table headers from the keys of the first object
    const headers = Object.keys(items[0]);
    const thead = document.createElement("thead");
    const headerRow = document.createElement("tr");

    headers.forEach(key => {
      const th = document.createElement("th");
      th.textContent = key.toUpperCase();
      th.style.borderBottom = "2px solid #ccc";
      th.style.padding = "8px";
      th.style.textAlign = "left";
      headerRow.appendChild(th);
    });
    thead.appendChild(headerRow);
    table.appendChild(thead);

    // Build data rows
    const tbody = document.createElement("tbody");
    items.forEach(item => {
      const row = document.createElement("tr");
      row.style.borderBottom = "1px solid #eee";
      row.style.cursor = this.onItemClick ? "pointer" : "default";

      if (this.onItemClick) {
        row.addEventListener("click", () => this.onItemClick(item));
      }

      headers.forEach(key => {
        const td = document.createElement("td");
        td.style.padding = "8px";
        
        // Format arrays (like permissions) cleanly
        const value = item[key];
        td.textContent = Array.isArray(value) ? value.join(", ") : value;
        
        row.appendChild(td);
      });
      tbody.appendChild(row);
    });

    table.appendChild(tbody);
    return table;
  }

  // Renders a key-value detail card for a single object
  _renderDetailView(item) {
    const card = document.createElement("div");
    card.style.border = "1px solid #ddd";
    card.style.borderRadius = "8px";
    card.style.padding = "16px";
    card.style.backgroundColor = "#f9f9f9";

    const keys = Object.keys(item);
    keys.forEach(key => {
      const row = document.createElement("div");
      row.style.marginBottom = "10px";

      const label = document.createElement("strong");
      label.textContent = `${key}: `;

      const valueSpan = document.createElement("span");
      const val = item[key];
      valueSpan.textContent = Array.isArray(val) ? val.join(", ") : val;

      row.appendChild(label);
      row.appendChild(valueSpan);
      card.appendChild(row);
    });

    return card;
  }
}