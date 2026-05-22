"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";

export default function Home() {
  const router = useRouter();
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const verifySession = async () => {
      try {
        const res = await fetch(`/api/me`, {
          method: "GET",
          credentials: "include",
          headers: {
            "Content-Type": "application/json",
          },
        });

        if (res.ok) {
          const user = await res.json();
          if (user.role === "Admin") router.push("/kebun");
          else if (user.role === "Mandor") router.push("/harvest/mandor");
          else if (user.role === "Supir") router.push("/delivery/supir");
          else router.push("/harvest/buruh/lapor");
          return;
        }
      } catch (error) {
        console.error("Gagal mengecek sesi:", error);
      }
      setIsLoading(false);
    };

    verifySession();
  }, [router]);

  if (isLoading) {
    return <div className="min-h-screen bg-slate-900 selection:bg-teal-500/30"></div>;
  }

  return (
      <div className="relative flex min-h-screen flex-col items-center justify-center overflow-hidden bg-slate-900 selection:bg-teal-500/30">
        {/* Decorative background elements */}
        <div className="absolute -top-40 -right-40 h-96 w-96 rounded-full bg-teal-500/20 blur-[100px]"></div>
        <div className="absolute -bottom-40 -left-40 h-96 w-96 rounded-full bg-emerald-500/20 blur-[100px]"></div>

        <div className="relative z-10 w-full max-w-2xl p-8 sm:p-12 text-center backdrop-blur-xl bg-white/5 border border-white/10 shadow-2xl rounded-3xl transition-all duration-300 hover:shadow-teal-500/10">
          <h1 className="text-4xl sm:text-5xl font-extrabold tracking-tight text-white mb-6">
            Selamat Datang di <span className="text-transparent bg-clip-text bg-gradient-to-r from-teal-400 to-emerald-400">MySawit</span>
          </h1>
          <p className="text-lg text-slate-400 mb-10 max-w-xl mx-auto">
            Platform Manajemen Kebun Sawit Terintegrasi. Pantau hasil panen, kelola pekerja, dan tingkatkan efisiensi perkebunan Anda.
          </p>

          <div className="flex flex-col sm:flex-row justify-center items-center gap-4 mb-10">
            <Link
                href="/auth/login"
                className="w-full sm:w-auto px-8 py-3.5 bg-teal-500 text-white font-semibold rounded-xl transition-all hover:bg-teal-400 focus:ring-2 focus:ring-teal-500 focus:ring-offset-2 focus:ring-offset-slate-900"
            >
              Masuk Akun
            </Link>
            <Link
                href="/auth/register"
                className="w-full sm:w-auto px-8 py-3.5 bg-white/10 border border-white/20 text-white font-semibold rounded-xl transition-all hover:bg-white/20 focus:ring-2 focus:ring-white/50 focus:ring-offset-2 focus:ring-offset-slate-900"
            >
              Daftar Baru
            </Link>
          </div>
        </div>
      </div>
  );
}