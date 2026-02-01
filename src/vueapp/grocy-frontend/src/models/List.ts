export interface List {
    id: string,
    title: string,
    archived: string,
    stats: ListStats,
    isDefault: boolean
}

export interface ListStats{
    total: number,
    checked: number
}