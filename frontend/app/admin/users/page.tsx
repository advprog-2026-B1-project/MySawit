"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";

const API = "http://localhost:8080";

interface User {
    id: number;
    username: string;
    nama: string;
    email: string;
    role: string;
}

const ROLES = ["Semua", "Admin", "Mandor", "Supir", "Buruh"];

const roleBadge = (role: string) => {
    const map: Record<string, string> = {
        Admin:  "bg-verdant-soft text-verdant",
        Mandor: "bg-bone/10 text-bone/70",
        Supir:  "bg-bone/10 text-bone/70",
        Buruh:  "bg-bone/10 text-bone/70",
    };
    return map[role] ?? "bg-bone/10 text-bone/50";
};

export default function UserListPage() {
    const router = useRouter();
    const [users, setUsers] = useState<User[]>([]);
    const [search, setSearch] = useState("");
    const [roleFilter, setRoleFilter] = useState("Semua");
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [currentAdminId, setCurrentAdminId] = useState<number | null>(null);
    const [deletingId, setDeletingId] = useState<number | null>(null);

    useEffect(() => {
        const init = async () => {
            try {
                const meRes = await fetch(`${API}/api/me`, { credentials: "include" });
                if (meRes.status === 401) { router.push("/login"); return; }
                const me = await meRes.json();
                setCurrentAdminId(me.id);

                const res = await fetch(`${API}/api/admin/users`, { credentials: "include" });
                if (!res.ok) { setError(`Gagal memuat data (${res.status})`); return; }
                setUsers(await res.json());
            } catch {
                setError("Tidak dapat terhubung ke server.");
            } finally {
                setLoading(false);
            }
        };
        init();
    }, [router]);

    const handleDelete = async (id: number) => {
        if (!confirm("Hapus user ini? Aksi tidak dapat dibatalkan.")) return;
        if (!currentAdminId) return;
        setDeletingId(id);
        try {
            const res = await fetch(
                `${API}/api/admin/users/${id}?currentAdminId=${currentAdminId}`,
                { method: "DELETE", credentials: "include" }
            );
            if (res.status === 400) {
                const data = await res.json().catch(() => ({}));
                alert(data.message || "Tidak dapat menghapus user ini.");
                return;
            }
            if (!res.ok) { alert(`Gagal menghapus (${res.status})`); return; }
            setUsers(prev => prev.filter(u => u.id !== id));
        } catch {
            alert("Tidak dapat terhubung ke server.");
        } finally {
            setDeletingId(null);
        }
    };

    const filtered = users.filter(u => {
        const matchRole = roleFilter === "Semua" || u.role === roleFilter;
        const q = search.toLowerCase();
        const matchSearch = !q ||
            u.nama?.toLowerCase().includes(q) ||
            u.email?.toLowerCase().includes(q) ||
            u.username?.toLowerCase().includes(q);
        return matchRole && matchSearch;
    });

    return (
        <div className="px-8 py-6">

            {/* Header */}
            <div className="flex items-center justify-between mb-6">
                <div>
                    <h1 className="text-xl font-semibold text-bone">Manajemen Pengguna</h1>
                    <p className="text-sm text-bone/40 mt-0.5">{users.length} pengguna terdaftar</p>
                </div>
                <Link
                    href="/admin/users/baru"
                    className="px-4 py-2 bg-verdant text-ink text-sm font-semibold rounded-md hover:bg-verdant-hover transition"
                >
                    + Tambah User
                </Link>
            </div>

            {/* Error */}
            {error && (
                <div className="mb-5 px-4 py-3 bg-red-500/10 border border-red-500/20 rounded-lg text-sm text-red-400">
                    {error}
                </div>
            )}

            {/* Filter bar */}
            <div className="flex gap-3 mb-5">
                <input
                    type="text"
                    placeholder="Cari nama, email, username..."
                    value={search}
                    onChange={e => setSearch(e.target.value)}
                    className="bg-ink-soft border border-line text-bone text-sm rounded-md px-3 py-2 w-72 focus:ring-1 focus:ring-verdant focus:border-verdant focus:outline-none placeholder:text-bone/30 transition"
                />
                <select
                    value={roleFilter}
                    onChange={e => setRoleFilter(e.target.value)}
                    className="bg-ink-soft border border-line text-bone text-sm rounded-md px-3 py-2 focus:ring-1 focus:ring-verdant focus:outline-none"
                >
                    {ROLES.map(r => <option key={r} value={r}>{r}</option>)}
                </select>
                {(search || roleFilter !== "Semua") && (
                    <button
                        onClick={() => { setSearch(""); setRoleFilter("Semua"); }}
                        className="text-xs text-bone/40 hover:text-bone transition px-2"
                    >
                        Reset
                    </button>
                )}
            </div>

            {/* Table */}
            <div className="rounded-lg border border-line overflow-hidden">
                <table className="min-w-full text-sm text-left">
                    <thead>
                        <tr className="bg-ink-soft border-b border-line">
                            <th className="px-5 py-3 text-xs font-semibold text-bone/40 uppercase tracking-wider">Nama</th>
                            <th className="px-5 py-3 text-xs font-semibold text-bone/40 uppercase tracking-wider">Username</th>
                            <th className="px-5 py-3 text-xs font-semibold text-bone/40 uppercase tracking-wider">Email</th>
                            <th className="px-5 py-3 text-xs font-semibold text-bone/40 uppercase tracking-wider">Role</th>
                            <th className="px-5 py-3 text-xs font-semibold text-bone/40 uppercase tracking-wider">Aksi</th>
                        </tr>
                    </thead>
                    <tbody className="divide-y divide-line">
                        {loading ? (
                            <tr>
                                <td colSpan={5} className="px-5 py-12 text-center text-bone/30 text-sm">
                                    Memuat data...
                                </td>
                            </tr>
                        ) : filtered.length === 0 ? (
                            <tr>
                                <td colSpan={5} className="px-5 py-12 text-center text-bone/30 text-sm">
                                    Tidak ada pengguna ditemukan.
                                </td>
                            </tr>
                        ) : (
                            filtered.map((user, i) => (
                                <tr
                                    key={user.id}
                                    className={`hover:bg-white/5 transition ${i % 2 === 1 ? "bg-white/[0.02]" : ""}`}
                                >
                                    <td className="px-5 py-3.5 font-medium text-bone">{user.nama ?? "-"}</td>
                                    <td className="px-5 py-3.5 text-bone/60 font-mono text-xs">{user.username ?? "-"}</td>
                                    <td className="px-5 py-3.5 text-bone/60">{user.email}</td>
                                    <td className="px-5 py-3.5">
                                        <span className={`px-2 py-0.5 rounded text-xs font-medium ${roleBadge(user.role)}`}>
                                            {user.role ?? "-"}
                                        </span>
                                    </td>
                                    <td className="px-5 py-3.5">
                                        <div className="flex items-center gap-2">
                                            <Link
                                                href={`/admin/users/${user.id}/edit`}
                                                className="px-2.5 py-1 text-xs border border-line text-bone/60 rounded hover:border-white/20 hover:text-bone transition"
                                            >
                                                Edit
                                            </Link>
                                            <button
                                                onClick={() => handleDelete(user.id)}
                                                disabled={deletingId === user.id || user.id === currentAdminId}
                                                className="px-2.5 py-1 text-xs text-red-400/70 hover:text-red-400 transition disabled:opacity-30 disabled:cursor-not-allowed"
                                                title={user.id === currentAdminId ? "Tidak dapat menghapus akun sendiri" : ""}
                                            >
                                                {deletingId === user.id ? "..." : "Hapus"}
                                            </button>
                                        </div>
                                    </td>
                                </tr>
                            ))
                        )}
                    </tbody>
                </table>
            </div>

            {/* Footer count */}
            {!loading && filtered.length > 0 && (
                <p className="mt-3 text-xs text-bone/30">
                    Menampilkan {filtered.length} dari {users.length} pengguna
                </p>
            )}
        </div>
    );
}
