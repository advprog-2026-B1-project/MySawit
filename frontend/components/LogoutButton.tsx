"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";

export default function LogoutButton() {
  const router = useRouter();
  const [isLoading, setIsLoading] = useState(false);

  const handleLogout = async () => {
    setIsLoading(true);
    try {
      const res = await fetch(`/api/logout`, { method: "POST", credentials: "include" });
      if (res.ok) {
        router.push("/auth/login");
        router.refresh();
      }
    } catch (e) {
      console.error(e);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <button
      onClick={handleLogout}
      disabled={isLoading}
      className="text-xs text-red-400 hover:text-red-300 transition-colors disabled:opacity-50"
    >
      {isLoading ? "Keluar..." : "Logout"}
    </button>
  );
}