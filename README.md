Markdown
# 🚀 FastPay: Enterprise Digital Wallet & P2P Ledger

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![JavaFX](https://img.shields.io/badge/JavaFX-FF0000?style=for-the-badge&logo=java&logoColor=white)
![Next.js](https://img.shields.io/badge/Next.js-000000?style=for-the-badge&logo=nextdotjs&logoColor=white)
![Supabase](https://img.shields.io/badge/Supabase-3ECF8E?style=for-the-badge&logo=supabase&logoColor=white)

FastPay is a high-performance, desktop-first digital wallet ecosystem. It combines a highly responsive **JavaFX rich-client** with an edge-rendered **Next.js web portal** to deliver secure peer-to-peer (P2P) transfers, virtual budget partitions, and dynamic utility integrations.

Designed with enterprise fintech standards in mind, FastPay implements strict client-side validation, secure remote procedure calls (RPC), and immutable transaction snapshotting.

---

## ✨ Core Architecture & Features

### 🛡️ Atomic Double-Entry Ledger
Financial integrity is paramount. While the frontend handles the UX, the architecture is designed to interface with an atomic double-entry database ledger (via Supabase/PostgreSQL), ensuring that funds are never artificially created or destroyed during network drops.

### 💼 Virtual Partitions (Budget Envelopes)
Users can split their main balance into distinct virtual partitions (e.g., Petrol, Groceries, Savings). The JavaFX UI utilizes optimized memory management to seamlessly render these partitions as floating, interactive cards.

### 🔗 Next.js Zero-Trust Receipt Portal
When a user shares a transaction via WhatsApp, FastPay generates a secure cryptographic UUID. The link routes to our Vercel-hosted Next.js web portal. 
* **Edge-Rendered Security:** Uses Supabase Service Role keys to fetch transaction data securely, bypassing standard RLS for public receipt viewing without exposing the broader database.
* **DOM Snapshotting:** Utilizes `html2canvas` for precise, client-side rendering of downloadable PNG receipts, isolated from the UI controls.

### 🏦 Dynamic RPC-Ready Biller Integrations
The "Pay Bills" and "Donations" modules feature a highly scalable Split-Panel architecture. 
* **Real-time Filtering:** A custom Java Stream-backed search engine instantly filters through 26+ utility providers (LESCO, SSGC, 1Bill, etc.).
* **Memory-Efficient Grids:** Uses dynamically populated `FlowPane` containers, preventing memory leaks and keeping the application lightweight.

### 💳 PCI-Inspired Data Handling & Validation
* **Luhn Algorithm & Format Validation:** Real-time keystroke interception in JavaFX ensures that Raast IDs (Phone/IBAN) and 8-digit M-Tag IDs are structurally valid before any network payload is constructed.
* **Data Masking:** Transaction IDs are truncated and sensitive data is masked in the UI, adhering to core PCI-DSS compliance principles for frontend display.

---

## 🛠️ Technology Stack

### The Client (Desktop)
* **Language:** Java 17+
* **UI Framework:** JavaFX (FXML)
* **Styling:** Modular CSS with custom pseudo-classes (`-fx-effect`, dynamic rendering)
* **Architecture:** MVC (Model-View-Controller)

### The Web Portal
* **Framework:** Next.js 14 (App Router)
* **Language:** JavaScript / TypeScript
* **SEO:** Built-in Meta Object mapping for targeted long-tail fintech keywords
* **Deployment:** Vercel Global Edge Network

### The Backend
* **Database:** Supabase (PostgreSQL)
* **Authentication:** JWT-based stateless sessions

---

## 🚀 Getting Started

### Prerequisites
* Java Development Kit (JDK) 17 or higher
* Maven or Gradle
* Node.js 18.x + (for the web portal)

### Running the JavaFX Desktop Client
1. Clone the repository:
   ```bash
   git clone [https://github.com/yourusername/fastpay.git](https://github.com/yourusername/fastpay.git)
2. Navigate to the desktop client directory and compile:
   ```bash
   mvn clean install

3. Run the application:
   ```bash
   mvn javafx:run
 4.Running the Web Portal (Next.js)
  Navigate to the fastpay-web-portal repository and clone.

 5. Install dependencies:
    ```Bash
    npm install
 6. Set up your .env.local with your Supabase keys:
    ```Code snippet
    NEXT_PUBLIC_SUPABASE_URL=your_url
    SUPABASE_SERVICE_ROLE_KEY=your_secret_key

 7. Start the development server:
    ```Bash
    npm run dev

📱 UI Gallery
The JavaFX UI features a custom-built, responsive split-pane navigation system, dynamic toggle groups for mobile network selection (Jazz, Zong, Telenor),
and seamless visual states for hover and selection events.

🔒 Security Notice
This project is an educational demonstration of fintech UI/UX principles and system architecture. Before deploying any real-world financial application,
ensure full compliance with regional financial regulations, PCI-DSS standards, and undergo professional penetration testing.
