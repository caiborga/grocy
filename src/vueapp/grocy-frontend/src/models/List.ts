export interface List {
    id: string,
    title: string,
    archived: boolean,
    stats: ListStats,
    isDefault: boolean
}

export interface ListStats{
    total: number,
    checked: number
}
