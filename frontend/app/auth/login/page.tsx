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
      const res = await fetch("/api/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
        body: JSON.stringify({ email, password }),
      });

      if (res.ok) {
        // Redirect to home or dashboard after successful login
        router.push("/");
      } else {
        const message = await res.text();
        setError(message || "Gagal login. Periksa kembali kredensial Anda.");
      }
    } catch {
      setError("Terjadi kesalahan pada server.");
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
