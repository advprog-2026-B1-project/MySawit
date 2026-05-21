import Link from "next/link";

const quickLinks = [
    {
        role: "Buruh",
        description: "Laporan hasil panen harian dan riwayat pengiriman",
        href: "/harvest/buruh/lapor",
        primary: true,
    },
    {
        role: "Mandor",
        description: "Review dan approval laporan panen dari buruh",
        href: "/harvest/mandor",
        primary: true,
    },
    {
        role: "Admin",
        description: "Kelola data kebun, mandor, dan supir",
        href: "/kebun",
        primary: false,
    },
];

const stats = [
    { label: "Modul Aktif", value: "3" },
    { label: "Endpoint API", value: "12+" },
    { label: "Role Pengguna", value: "4" },
];

export default function Home() {
    return (
        <div className="min-h-full px-8 py-12 max-w-3xl">

            {/* Header */}
            <div className="mb-12">
                <p className="text-xs font-semibold text-verdant uppercase tracking-wider mb-3">
                    Platform Manajemen
                </p>
                <h1 className="text-4xl font-bold text-bone mb-4 leading-tight">
                    Selamat datang di<br />
                    <span className="text-verdant">MySawit</span>
                </h1>
                <p className="text-bone/50 text-base max-w-md">
                    Sistem terintegrasi untuk manajemen kebun sawit — dari pencatatan panen hingga pengiriman hasil produksi.
                </p>
            </div>

            {/* Stats row */}
            <div className="flex gap-6 mb-12">
                {stats.map((s) => (
                    <div key={s.label} className="flex flex-col">
                        <span className="text-2xl font-bold text-bone">{s.value}</span>
                        <span className="text-xs text-bone/40 mt-0.5">{s.label}</span>
                    </div>
                ))}
            </div>

            {/* Role cards */}
            <div className="space-y-3">
                <p className="text-xs font-semibold text-bone/30 uppercase tracking-wider mb-4">Masuk sebagai</p>
                {quickLinks.map((link) => (
                    <Link
                        key={link.role}
                        href={link.href}
                        className="flex items-center justify-between px-5 py-4 rounded-lg border border-white/10 bg-ink-muted hover:border-verdant/30 hover:bg-verdant-soft transition group"
                    >
                        <div>
                            <p className="text-sm font-semibold text-bone group-hover:text-verdant transition">
                                {link.role}
                            </p>
                            <p className="text-xs text-bone/40 mt-0.5">{link.description}</p>
                        </div>
                        <span className="text-bone/20 group-hover:text-verdant transition text-lg">→</span>
                    </Link>
                ))}
            </div>
        </div>
    );
}
