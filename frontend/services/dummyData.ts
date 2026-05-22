import { Delivery } from "./deliveryApi";

// ── Realistic dummy data ────────────────────────────────────────────
export const DUMMY_DELIVERIES: Delivery[] = [
    {
        id: 1,
        driver: { id: 2, nama: "Budi Santoso", username: "budi_supir" },
        mandor: { id: 1, nama: "Pak Harto", username: "harto_mandor" },
        hasilPanen: { id: 10, kilogram: 250, tanggalPanen: "2026-05-18", kebun: { namaKebun: "Kebun Sawit Jaya" } },
        status: "Memuat", mandorDecision: "Pending", adminDecision: "Pending",
        createdAt: "2026-05-18T08:00:00+07:00", updatedAt: "2026-05-18T08:00:00+07:00",
    },
    {
        id: 2,
        driver: { id: 3, nama: "Agus Prabowo", username: "agus_supir" },
        mandor: { id: 1, nama: "Pak Harto", username: "harto_mandor" },
        hasilPanen: { id: 11, kilogram: 180, tanggalPanen: "2026-05-17", kebun: { namaKebun: "Kebun Makmur Sejahtera" } },
        status: "Mengirim", mandorDecision: "Pending", adminDecision: "Pending",
        createdAt: "2026-05-17T10:00:00+07:00", updatedAt: "2026-05-18T06:30:00+07:00",
    },
    {
        id: 3,
        driver: { id: 2, nama: "Budi Santoso", username: "budi_supir" },
        mandor: { id: 1, nama: "Pak Harto", username: "harto_mandor" },
        hasilPanen: { id: 12, kilogram: 320, tanggalPanen: "2026-05-16", kebun: { namaKebun: "Kebun Sawit Jaya" } },
        status: "Tiba", mandorDecision: "Pending", adminDecision: "Pending",
        arrivedAt: "2026-05-17T14:00:00+07:00",
        createdAt: "2026-05-16T07:00:00+07:00", updatedAt: "2026-05-17T14:00:00+07:00",
    },
    {
        id: 4,
        driver: { id: 4, nama: "Dedi Kurniawan", username: "dedi_supir" },
        mandor: { id: 1, nama: "Pak Harto", username: "harto_mandor" },
        hasilPanen: { id: 13, kilogram: 150, tanggalPanen: "2026-05-15", kebun: { namaKebun: "Kebun Makmur Sejahtera" } },
        status: "Tiba", mandorDecision: "Approved", adminDecision: "Pending",
        arrivedAt: "2026-05-16T11:00:00+07:00", mandorDecidedAt: "2026-05-16T12:00:00+07:00",
        createdAt: "2026-05-15T09:00:00+07:00", updatedAt: "2026-05-16T12:00:00+07:00",
    },
    {
        id: 5,
        driver: { id: 3, nama: "Agus Prabowo", username: "agus_supir" },
        mandor: { id: 5, nama: "Pak Wiranto", username: "wiranto_mandor" },
        hasilPanen: { id: 14, kilogram: 200, tanggalPanen: "2026-05-14", kebun: { namaKebun: "Kebun Subur Abadi" } },
        status: "Tiba", mandorDecision: "Approved", adminDecision: "Pending",
        arrivedAt: "2026-05-15T10:00:00+07:00", mandorDecidedAt: "2026-05-15T11:00:00+07:00",
        createdAt: "2026-05-14T08:00:00+07:00", updatedAt: "2026-05-15T11:00:00+07:00",
    },
    {
        id: 6,
        driver: { id: 2, nama: "Budi Santoso", username: "budi_supir" },
        mandor: { id: 1, nama: "Pak Harto", username: "harto_mandor" },
        hasilPanen: { id: 15, kilogram: 100, tanggalPanen: "2026-05-13", kebun: { namaKebun: "Kebun Sawit Jaya" } },
        status: "Tiba", mandorDecision: "Rejected", adminDecision: "Pending",
        mandorRejectionReason: "Buah mentah berlebih, kualitas tidak memenuhi standar",
        arrivedAt: "2026-05-14T09:00:00+07:00", mandorDecidedAt: "2026-05-14T10:00:00+07:00",
        createdAt: "2026-05-13T07:00:00+07:00", updatedAt: "2026-05-14T10:00:00+07:00",
    },
    {
        id: 7,
        driver: { id: 4, nama: "Dedi Kurniawan", username: "dedi_supir" },
        mandor: { id: 5, nama: "Pak Wiranto", username: "wiranto_mandor" },
        hasilPanen: { id: 16, kilogram: 280, tanggalPanen: "2026-05-12", kebun: { namaKebun: "Kebun Subur Abadi" } },
        status: "Tiba", mandorDecision: "Approved", adminDecision: "Approved",
        acknowledgedKg: 280,
        arrivedAt: "2026-05-13T13:00:00+07:00", mandorDecidedAt: "2026-05-13T14:00:00+07:00", adminDecidedAt: "2026-05-13T15:00:00+07:00",
        createdAt: "2026-05-12T06:00:00+07:00", updatedAt: "2026-05-13T15:00:00+07:00",
    },
    {
        id: 8,
        driver: { id: 3, nama: "Agus Prabowo", username: "agus_supir" },
        mandor: { id: 1, nama: "Pak Harto", username: "harto_mandor" },
        hasilPanen: { id: 17, kilogram: 350, tanggalPanen: "2026-05-11", kebun: { namaKebun: "Kebun Makmur Sejahtera" } },
        status: "Tiba", mandorDecision: "Approved", adminDecision: "PartiallyApproved",
        acknowledgedKg: 300, adminRejectionReason: "50 Kg rusak / busuk saat tiba di pabrik",
        arrivedAt: "2026-05-12T12:00:00+07:00", mandorDecidedAt: "2026-05-12T13:00:00+07:00", adminDecidedAt: "2026-05-12T14:30:00+07:00",
        createdAt: "2026-05-11T08:00:00+07:00", updatedAt: "2026-05-12T14:30:00+07:00",
    },
];

export const DUMMY_DRIVERS = [
    { id: 2, nama: "Budi Santoso" },
    { id: 3, nama: "Agus Prabowo" },
    { id: 4, nama: "Dedi Kurniawan" },
];

export const DUMMY_HARVESTS = [
    { id: 20, kilogram: 250, status: "Approved", tanggalPanen: "2026-05-18", kebun: { namaKebun: "Kebun Sawit Jaya" } },
    { id: 21, kilogram: 180, status: "Approved", tanggalPanen: "2026-05-17", kebun: { namaKebun: "Kebun Makmur Sejahtera" } },
    { id: 22, kilogram: 150, status: "Approved", tanggalPanen: "2026-05-16", kebun: { namaKebun: "Kebun Subur Abadi" } },
];
