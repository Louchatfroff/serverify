import { Inter, Inconsolata, Open_Sans } from "next/font/google";
import "./globals.css";

const display = Inter({ subsets: ["latin"], variable: "--font-display", display: "swap" });
const body = Open_Sans({ subsets: ["latin"], variable: "--font-body", display: "swap" });
const mono = Inconsolata({ subsets: ["latin"], variable: "--font-mono", display: "swap" });

export const metadata = {
  title: "Serverify",
  description: "Browse, search and join Minecraft servers without leaving the game. A client-side mod for Fabric, Quilt, Forge and NeoForge.",
};

export default function RootLayout({ children }) {
  return (
    <html lang="en" className={`${display.variable} ${body.variable} ${mono.variable}`}>
      <body>{children}</body>
    </html>
  );
}
