import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import Link from "next/link";
import "./globals.css";

const geistSans = Geist({
    variable: "--font-geist-sans",
    subsets: ["latin"],
});

const geistMono = Geist_Mono({
    variable: "--font-geist-mono",
    subsets: ["latin"],
});

export const metadata: Metadata = {
    title: "MySawit - Platform Manajemen Kebun",
    description: "Aplikasi manajemen panen sawit terintegrasi",
};

const navLinks = [
    {
        group: "Admin",
        items: [
            { label: "Manajemen User", href: "/admin/users" },
            { label: "Assign Buruh", href: "/admin/users/assign" },
        ],
    },
    {
        group: "Kebun",
        items: [
            { label: "Daftar Kebun", href: "/kebun" },
        ],
    },
    {
        group: "Panen",
        items: [
            { label: "Lapor Panen", href: "/harvest/buruh/lapor" },
            { label: "Riwayat Panen", href: "/harvest/buruh/riwayat" },
            { label: "Dashboard Mandor", href: "/harvest/mandor" },
        ],
    },
    {
        group: "Pengiriman",
        items: [
            { label: "Tugas Supir", href: "/delivery/supir" },
            { label: "Manajemen Mandor", href: "/delivery/mandor" },
            { label: "Review Admin", href: "/delivery/admin" },
        ],
    },
];

export default function RootLayout({
    children,
}: Readonly<{ children: React.ReactNode }>) {
    return (
        <html lang="id">
        <body className={`${geistSans.variable} ${geistMono.variable} antialiased`}>
        <div className="flex h-screen bg-ink text-bone overflow-hidden">

            {/* Sidebar */}
            <aside className="w-56 shrink-0 flex flex-col bg-ink-soft border-r border-line overflow-y-auto">

                {/* Logo */}
                <div className="px-5 py-5 border-b border-line">
                    <Link href="/" className="block">
                        <p className="text-verdant font-bold text-lg tracking-tight">MySawit</p>
                        <p className="text-bone/40 text-xs mt-0.5">Platform Kebun Sawit</p>
                    </Link>
                </div>

                {/* Nav */}
                <nav className="flex-1 px-3 py-4 space-y-5">
                    {navLinks.map((group) => (
                        <div key={group.group}>
                            <p className="px-2 mb-1.5 text-xs font-semibold text-bone/30 uppercase tracking-wider">
                                {group.group}
                            </p>
                            <ul className="space-y-0.5">
                                {group.items.map((item) => (
                                    <li key={item.href}>
                                        <Link
                                            href={item.href}
                                            className="flex items-center gap-2 px-2 py-1.5 rounded-md text-sm text-bone/70 hover:text-bone hover:bg-verdant-dim transition"
                                        >
                                            {item.label}
                                        </Link>
                                    </li>
                                ))}
                            </ul>
                        </div>
                    ))}
                </nav>

                {/* Footer */}
                <div className="px-5 py-4 border-t border-line">
                    <p className="text-xs text-bone/30">Kelompok B1 · 2026</p>
                </div>
            </aside>

            {/* Main content */}
            <main className="flex-1 overflow-auto">
                {children}
            </main>

        </div>
        </body>
        </html>
    );
}
