

export class Logger {
  // Purple for business/domain expected errors (e.g., bad credentials, invalid forms)
  static domainError(message, details = "") {
    console.log(
      `%c 💜 APP NOTICE: ${message} `,
      "background-color: #8A2BE2; color: #FFFFFF; font-weight: bold; padding: 3px 6px; border-radius: 3px;",
      details
    );
  }

  // Cyan/Blue for informative state changes
  static info(message) {
    console.log(
      `%c ℹ️ INFO: ${message} `,
      "background-color: #008B8B; color: #FFFFFF; font-weight: bold; padding: 3px 6px; border-radius: 3px;"
    );
  }
}