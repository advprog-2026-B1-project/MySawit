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

                {/* Input Berita */}
                <div className="flex flex-col gap-2">
                    <label className="font-semibold text-sm text-gray-700">Berita/Keterangan</label>
                    <textarea
                        className="border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500 w-full resize-none"
                        placeholder="Detail panen hari ini..."
                        value={berita}
                        onChange={(e) => setBerita(e.target.value)}
                        rows={3}
                        disabled={isLoading}
                    ></textarea>
                </div>

                {/* Drag and Drop Area */}
                <div className="flex flex-col gap-2">
                    <label className="font-semibold text-sm text-gray-700">Bukti Foto (Bisa lebih dari 1)</label>

                    <div
                        className={`border-2 border-dashed rounded-lg p-8 text-center transition-colors ${
                            isLoading ? "opacity-50 cursor-not-allowed" : "cursor-pointer"
                        } ${
                            isDragging ? "border-blue-500 bg-blue-50" : "border-gray-300 hover:bg-gray-50 bg-gray-50/50"
                        }`}
                        onDragOver={handleDragOver}
                        onDragLeave={handleDragLeave}
                        onDrop={handleDrop}
                        onClick={() => !isLoading && fileInputRef.current?.click()}
                    >
                        <p className="text-gray-500 text-sm">
                            Tarik dan lepas gambar di sini, atau <span className="text-blue-600 font-medium">klik untuk memilih file</span>
                        </p>
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

                    {/* Preview File */}
                    {files.length > 0 && (
                        <div className="mt-2 flex flex-col gap-2">
                            <span className="text-xs font-semibold text-gray-600 uppercase tracking-wider">File terpilih ({files.length}):</span>
                            <ul className="space-y-2">
                                {files.map((file, index) => (
                                    <li key={index} className="flex justify-between items-center bg-gray-100 px-3 py-2 rounded-md text-sm border border-gray-200">
                                        <span className="truncate max-w-[80%] text-gray-700">{file.name}</span>
                                        <button
                                            type="button"
                                            onClick={() => removeFile(index)}
                                            className="text-red-500 hover:text-red-700 font-medium text-xs px-2 py-1"
                                            disabled={isLoading}
                                        >
                                            Hapus
                                        </button>
                                    </li>
                                ))}
                            </ul>
                        </div>
                    )}
                </div>

                {/* Submit Button */}
                <button
                    type="submit"
                    className="mt-4 w-full bg-blue-600 text-white font-semibold py-2.5 rounded-md hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                    disabled={files.length === 0 || !kilogram || isLoading}
                >
                    {isLoading ? "Mengirim Laporan..." : "Kirim Laporan"}
                </button>
            </form>
        </div>
    );
}