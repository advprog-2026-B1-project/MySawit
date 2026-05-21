import Link from "next/link";

export default function Home() {
  return (
      <div className="flex min-h-screen flex-col items-center justify-center p-24 bg-gray-50">
        <h1 className="text-4xl font-bold text-green-700 mb-4">Selamat Datang di MySawit</h1>
        <p className="text-gray-600 mb-8">Platform Manajemen Kebun Sawit Terintegrasi</p>

        <div className="flex flex-wrap gap-4 justify-center">
          <Link href="/buruh/lapor" className="px-6 py-2 bg-green-600 text-white rounded-md hover:bg-green-700">
            Masuk sebagai Buruh
          </Link>
          <Link href="/mandor" className="px-6 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700">
            Masuk sebagai Mandor
          </Link>
          <Link href="/kebun" className="px-6 py-2 bg-gray-700 text-white rounded-md hover:bg-gray-800">
            Admin — Kelola Kebun
          </Link>
        </div>
      </div>
  );
}