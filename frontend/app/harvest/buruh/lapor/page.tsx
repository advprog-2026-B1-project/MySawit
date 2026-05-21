"use client";

import { useState, useRef } from "react";
import Link from "next/link";

export default function LaporPanenBuruh() {
    const [kilogram, setKilogram] = useState("");
    const [berita, setBerita] = useState("");
    const [files, setFiles] = useState<File[]>([]);

    const [isDragging, setIsDragging] = useState(false);
    const [isLoading, setIsLoading] = useState(false);

    const fileInputRef = useRef<HTMLInputElement>(null);

    const handleDragOver = (e: React.DragEvent) => {
        e.preventDefault();
        setIsDragging(true);
    };

    const handleDragLeave = (e: React.DragEvent) => {
        e.preventDefault();
        setIsDragging(false);
    };

    const handleDrop = (e: React.DragEvent) => {
        e.preventDefault();
        setIsDragging(false);
        if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
            const droppedFiles = Array.from(e.dataTransfer.files);
            setFiles((prev) => [...prev, ...droppedFiles]);
        }
    };

    const handleFileSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (e.target.files && e.target.files.length > 0) {
            const selectedFiles = Array.from(e.target.files);
            setFiles((prev) => [...prev, ...selectedFiles]);
        }
    };

    const removeFile = (indexToRemove: number) => {
        setFiles(files.filter((_, index) => index !== indexToRemove));
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setIsLoading(true);

        try {
            const formData = new FormData();
            formData.append("kilogram", kilogram);
            formData.append("berita", berita);

            files.forEach((file) => {
                formData.append("photos", file);
            });

            const response = await fetch("http://localhost:8080/api/harvest", {
                method: "POST",
                body: formData,
            });

            if (response.ok) {
                alert("Laporan panen berhasil dikirim!");
                setKilogram("");
                setBerita("");
                setFiles([]);
            } else {
                const errorData = await response.json().catch(() => null);
                alert(`Gagal mengirim laporan: ${errorData?.message || response.statusText}`);
            }
        } catch (error) {
            console.error("Terjadi kesalahan:", error);
            alert("Terjadi kesalahan koneksi ke server.");
        } finally {
            setIsLoading(false);
        }
    };

    const inputCls = "w-full bg-ink border border-white/10 text-bone text-sm rounded-md px-3 py-2 focus:ring-1 focus:ring-verdant focus:border-verdant focus:outline-none placeholder:text-bone/30 transition";

    return (
        <div className="p-6 max-w-2xl mx-auto text-gray-800">
            <div className="flex justify-between items-center mb-6">
                <h1 className="text-2xl font-bold text-gray-900">Form Laporan Hasil Panen</h1>

                <Link
                    href="/harvest/buruh/riwayat"
                    className="bg-gray-200 hover:bg-gray-300 text-gray-800 text-sm font-semibold py-2 px-4 rounded-md transition-colors"
                >
                    Lihat Riwayat
                </Link>
            </div>

            <form onSubmit={handleSubmit} className="flex flex-col gap-5 bg-white p-6 shadow-sm rounded-lg border border-gray-200">

                {/* Input Kilogram */}
                <div className="flex flex-col gap-2">
                    <label className="font-semibold text-sm text-gray-700">Kilogram Panen</label>
                    <input
                        type="number"
                        step="0.01"
                        className="border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500 w-full"
                        placeholder="Contoh: 150.5"
                        value={kilogram}
                        onChange={(e) => setKilogram(e.target.value)}
                        required
                        disabled={isLoading}
                    />
                </div>

            {/* Page header */}
            <div className="flex items-start justify-between mb-8">
                <div>
                    <h1 className="text-xl font-semibold text-bone">Lapor Panen</h1>
                    <p className="text-sm text-bone/40 mt-1">Kirim laporan hasil panen harian</p>
                </div>
                <Link
                    href="/harvest/buruh/riwayat"
                    className="px-3 py-1.5 text-xs border border-white/10 text-bone/60 rounded-md hover:border-white/20 hover:text-bone transition shrink-0"
                >
                    Lihat Riwayat
                </Link>
            </div>

            {/* Form card */}
            <form onSubmit={handleSubmit} className="bg-ink-muted border border-white/10 rounded-lg divide-y divide-white/5">

                {/* Fields */}
                <div className="px-6 py-5 space-y-5">

                    {/* Kilogram */}
                    <div>
                        <label className="block text-xs font-semibold text-bone/40 uppercase tracking-wider mb-1.5">
                            Kilogram Panen <span className="text-verdant/60">*</span>
                        </label>
                        <input
                            type="number"
                            step="0.01"
                            className={inputCls}
                            placeholder="Contoh: 150.5"
                            value={kilogram}
                            onChange={(e) => setKilogram(e.target.value)}
                            required
                            disabled={isLoading}
                        />
                    </div>

                    {/* Berita */}
                    <div>
                        <label className="block text-xs font-semibold text-bone/40 uppercase tracking-wider mb-1.5">
                            Berita / Keterangan
                        </label>
                        <textarea
                            className={`${inputCls} resize-none`}
                            placeholder="Detail panen hari ini..."
                            value={berita}
                            onChange={(e) => setBerita(e.target.value)}
                            rows={3}
                            disabled={isLoading}
                        />
                    </div>

                    {/* Upload */}
                    <div>
                        <label className="block text-xs font-semibold text-bone/40 uppercase tracking-wider mb-1.5">
                            Bukti Foto <span className="text-verdant/60">*</span>
                        </label>

                        <div
                            className={`border-2 border-dashed rounded-lg px-6 py-8 text-center transition ${
                                isLoading ? "opacity-50 cursor-not-allowed" : "cursor-pointer"
                            } ${
                                isDragging
                                    ? "border-verdant bg-verdant-soft"
                                    : "border-white/10 hover:border-verdant/30 hover:bg-white/[0.02]"
                            }`}
                            onDragOver={handleDragOver}
                            onDragLeave={handleDragLeave}
                            onDrop={handleDrop}
                            onClick={() => !isLoading && fileInputRef.current?.click()}
                        >
                            <p className="text-2xl mb-2 text-bone/20">↑</p>
                            <p className="text-sm text-bone/40">
                                Tarik file ke sini, atau{" "}
                                <span className="text-verdant font-medium">klik untuk memilih</span>
                            </p>
                            <p className="text-xs text-bone/20 mt-1">PNG, JPG, WEBP</p>
                            <input
                                type="file"
                                multiple
                                accept="image/*"
                                className="hidden"
                                ref={fileInputRef}
                                onChange={handleFileSelect}
                                disabled={isLoading}
                            />
                        </div>

                        {/* File list */}
                        {files.length > 0 && (
                            <div className="mt-3 space-y-1.5">
                                <p className="text-xs text-bone/30 uppercase tracking-wider">
                                    {files.length} file dipilih
                                </p>
                                {files.map((file, index) => (
                                    <div
                                        key={index}
                                        className="flex items-center justify-between px-3 py-2 bg-ink border border-white/5 rounded-md text-sm group"
                                    >
                                        <div className="flex items-center gap-2 min-w-0">
                                            <span className="text-bone/20 shrink-0">▪</span>
                                            <span className="truncate text-bone/60 text-xs">{file.name}</span>
                                        </div>
                                        <button
                                            type="button"
                                            onClick={() => removeFile(index)}
                                            className="text-bone/20 hover:text-red-400 transition text-xs ml-2 shrink-0"
                                            disabled={isLoading}
                                        >
                                            ✕
                                        </button>
                                    </div>
                                ))}
                            </div>
                        )}
                    </div>
                </div>

                {/* Footer */}
                <div className="px-6 py-4 bg-ink-soft rounded-b-lg flex items-center justify-between">
                    <p className="text-xs text-bone/25">
                        {files.length === 0 ? "Minimal 1 foto wajib dilampirkan" : `${files.length} foto siap dikirim`}
                    </p>
                    <button
                        type="submit"
                        className="px-5 py-2 bg-verdant text-ink text-sm font-semibold rounded-md hover:bg-verdant-hover disabled:opacity-40 disabled:cursor-not-allowed transition"
                        disabled={files.length === 0 || !kilogram || isLoading}
                    >
                        {isLoading ? "Mengirim..." : "Kirim Laporan"}
                    </button>
                </div>
            </form>
        </div>
    );
}
