"use client";
import { useState } from 'react';

export default function CreateHarvestForm() {
    const [kilogram, setKilogram] = useState('');
    const [berita, setBerita] = useState('');
    const [message, setMessage] = useState('');

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        try {
            const res = await fetch('http://localhost:8080/api/harvest', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ kilogram: Number(kilogram), berita })
            });

            if (!res.ok) {
                const errorData = await res.json();
                throw new Error(errorData.message || "Gagal menyimpan data.");
            }

            setMessage("Hasil panen berhasil disimpan!");
            setKilogram('');
            setBerita('');
        } catch (error: any) {
            setMessage(error.message || "Buruh maksimal hanya bisa input sekali hari.");
        }
    };

    return (
        <div className="max-w-md mx-auto mt-10 p-6 bg-white rounded-lg shadow-md">
            <h2 className="text-2xl font-bold mb-4 text-black">Input Hasil Panen</h2>
            {message && (
                <div className={`p-3 mb-4 rounded ${message.includes('berhasil') ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'}`}>
                    {message}
                </div>
            )}
            <form onSubmit={handleSubmit} className="space-y-4">
                <div>
                    <label className="block text-black text-sm font-medium mb-1">Kilogram:</label>
                    <input
                        type="number"
                        value={kilogram}
                        onChange={e => setKilogram(e.target.value)}
                        className="w-full border p-2 rounded text-black"
                        required
                    />
                </div>
                <div>
                    <label className="block text-black text-sm font-medium mb-1">Berita:</label>
                    <textarea
                        value={berita}
                        onChange={e => setBerita(e.target.value)}
                        className="w-full border p-2 rounded h-24 text-black"
                        required
                    />
                </div>
                <button type="submit" className="w-full bg-blue-600 hover:bg-blue-700 text-white p-2 rounded">
                    Kirim Laporan
                </button>
            </form>
        </div>
    );
}