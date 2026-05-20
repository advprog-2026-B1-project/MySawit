"use client";

import { useEffect, useState } from "react";

interface User {
  id: number;
  nama: string;
  email: string;
  role: string;
}

export default function AdminUserManagement() {
  const [users, setUsers] = useState<User[]>([]);
  // Asumsi ID admin yang sedang login adalah 1 untuk demonstrasi
  const currentAdminId = 1; 

  const fetchUsers = async () => {
    try {
      const res = await fetch("http://localhost:8080/api/admin/users");
      if (res.ok) {
        const data = await res.json();
        setUsers(data);
      }
    } catch (err) {
      console.error("Gagal mengambil data pengguna", err);
    }
  };

  useEffect(() => {
    fetchUsers();
  }, []);

  const handleDelete = async (id: number) => {
    if (!confirm("Yakin ingin menghapus pengguna ini?")) return;
    
    try {
      const res = await fetch(`http://localhost:8080/api/admin/users/${id}?currentAdminId=${currentAdminId}`, {
        method: "DELETE",
      });

      if (res.ok) {
        setUsers(users.filter((u) => u.id !== id));
      } else {
        const errorText = await res.text();
        alert(`Gagal menghapus: ${errorText}`);
      }
    } catch (err) {
      console.error(err);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 p-8">
      <div className="mx-auto max-w-5xl rounded-xl bg-white p-6 shadow-sm">
        <div className="mb-6 flex items-center justify-between">
          <h1 className="text-2xl font-bold text-gray-800">Manajemen Pengguna</h1>
          <button className="rounded-lg bg-green-600 px-4 py-2 text-white hover:bg-green-700">
            + Tambah Pengguna
          </button>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm text-gray-600">
            <thead className="bg-green-50 text-green-800 uppercase">
              <tr>
                <th className="px-4 py-3">Nama</th>
                <th className="px-4 py-3">Email</th>
                <th className="px-4 py-3">Peran</th>
                <th className="px-4 py-3 text-right">Aksi</th>
              </tr>
            </thead>
            <tbody>
              {users.map((user) => (
                <tr key={user.id} className="border-b hover:bg-gray-50">
                  <td className="px-4 py-3 font-medium text-gray-900">{user.nama}</td>
                  <td className="px-4 py-3">{user.email}</td>
                  <td className="px-4 py-3">
                    <span className="rounded-full bg-green-100 px-2 py-1 text-xs font-semibold text-green-800">
                      {user.role}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-right space-x-2">
                    <button className="text-blue-600 hover:underline">Edit</button>
                    <button 
                      onClick={() => handleDelete(user.id)}
                      className="text-red-600 hover:underline"
                    >
                      Hapus
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          {users.length === 0 && (
            <p className="py-4 text-center text-gray-500">Belum ada data pengguna.</p>
          )}
        </div>
      </div>
    </div>
  );
}