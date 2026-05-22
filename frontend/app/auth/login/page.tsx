"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";

export default function LoginPage() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const router = useRouter();

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    setIsLoading(true);

    try {
      const res = await fetch(`/api/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
        body: JSON.stringify({ email, password }),
      });

      if (res.ok) {
        const meRes = await fetch(`/api/me`, {
          method: "GET",
          credentials: "include",
          headers: { "Content-Type": "application/json" },
        });

        if (meRes.ok) {
          const user = await meRes.json();

          if (user.role === "Admin") router.push("/kebun");
          else if (user.role === "Mandor") router.push("/harvest/mandor");
          else if (user.role === "Supir") router.push("/delivery/supir");
          else router.push("/harvest/buruh/lapor");
        } else {
          router.push("/");
        }
      } else {
        const message = await res.text();
        setError(message || "Gagal login. Periksa kembali kredensial Anda.");
      }
    } catch {
      setError("Terjadi kesalahan pada server. Pastikan backend menyala.");
    } finally {
      setIsLoading(false);
    }
  };

  return (
      <div className="relative flex min-h-screen items-center justify-center overflow-hidden bg-slate-900 selection:bg-teal-500/30">
        {/* Decorative background elements */}
        <div className="absolute -top-40 -right-40 h-96 w-96 rounded-full bg-teal-500/20 blur-[100px]"></div>
        <div className="absolute -bottom-40 -left-40 h-96 w-96 rounded-full bg-emerald-500/20 blur-[100px]"></div>

        <div className="relative z-10 w-full max-w-md p-8 sm:p-10 backdrop-blur-xl bg-white/5 border border-white/10 shadow-2xl rounded-3xl transition-all duration-300 hover:shadow-teal-500/10">
          <div className="mb-10 text-center">
            <h2 className="text-3xl font-extrabold tracking-tight text-white sm:text-4xl">
              Welcome Back
            </h2>
            <p className="mt-3 text-sm text-slate-400">
              Sign in to continue to <span className="font-semibold text-teal-400">MySawit</span>
            </p>
          </div>

          {error && (
              <div className="mb-6 rounded-lg bg-red-500/10 border border-red-500/20 p-4 text-sm text-red-400 flex items-center gap-3">
                <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 flex-shrink-0" viewBox="0 0 20 20" fill="currentColor">
                  <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
                </svg>
                <p>{error}</p>
              </div>
          )}

          <form onSubmit={handleLogin} className="space-y-6">
            <div className="space-y-1.5">
              <label className="block text-sm font-medium text-slate-300">Email Address</label>
              <div className="relative">
                <input
                    type="email"
                    required
                    className="w-full rounded-xl border border-white/10 bg-white/5 px-4 py-3.5 text-white placeholder-slate-500 transition-all focus:border-teal-400 focus:bg-white/10 focus:outline-none focus:ring-2 focus:ring-teal-400/20"
                    placeholder="name@example.com"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                />
              </div>
            </div>

            <div className="space-y-1.5">
              <div className="flex items-center justify-between">
                <label className="block text-sm font-medium text-slate-300">Password</label>
                <a href="#" className="text-xs font-medium text-teal-400 transition-colors hover:text-teal-300">Forgot password?</a>
              </div>
              <div className="relative">
                <input
                    type="password"
                    required
                    className="w-full rounded-xl border border-white/10 bg-white/5 px-4 py-3.5 text-white placeholder-slate-500 transition-all focus:border-teal-400 focus:bg-white/10 focus:outline-none focus:ring-2 focus:ring-teal-400/20"
                    placeholder="••••••••"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                />
              </div>
            </div>

            <button
                type="submit"
                disabled={isLoading}
                className="group relative flex w-full justify-center overflow-hidden rounded-xl bg-teal-500 px-4 py-3.5 text-sm font-bold text-white transition-all hover:bg-teal-400 focus:outline-none focus:ring-2 focus:ring-teal-500 focus:ring-offset-2 focus:ring-offset-slate-900 disabled:opacity-70"
            >
            <span className="relative z-10 flex items-center gap-2">
              {isLoading ? (
                  <>
                    <svg className="h-4 w-4 animate-spin text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                    </svg>
                    Signing in...
                  </>
              ) : (
                  'Sign in'
              )}
            </span>
            </button>
          </form>

          {/* --- BAGIAN TOMBOL GOOGLE DITAMBAHKAN DI SINI --- */}
          <div className="mt-6 flex items-center justify-center space-x-4">
            <span className="h-px w-full bg-white/10"></span>
            <span className="text-xs font-medium text-slate-500 uppercase tracking-wider">Or</span>
            <span className="h-px w-full bg-white/10"></span>
          </div>

          <div className="mt-6">
            <a
                href="/oauth2/authorization/google"
                className="flex w-full items-center justify-center gap-3 rounded-xl border border-white/10 bg-white/5 px-4 py-3.5 text-sm font-semibold text-white transition-all hover:bg-white/10 hover:border-white/20 focus:outline-none focus:ring-2 focus:ring-white/20"
            >
              <svg className="h-5 w-5" viewBox="0 0 24 24">
                <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="#4285F4" />
                <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853" />
                <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" fill="#FBBC05" />
                <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335" />
              </svg>
              Sign in with Google
            </a>
          </div>
          {/* ----------------------------------------------- */}

          <p className="mt-8 text-center text-sm text-slate-400">
            Don&apos;t have an account?{' '}
            <Link href="/auth/register" className="font-semibold text-teal-400 transition-colors hover:text-teal-300">
              Sign up now
            </Link>
          </p>
        </div>
      </div>
  );
}