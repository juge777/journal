export interface Diary {
  id: number
  title: string | null
  content: string
  mood: string | null
  weather: string | null
  diaryDate: string
  createdAt: string
  updatedAt: string
}

export interface DiaryRequest {
  title?: string
  content: string
  mood?: string
  weather?: string
  diaryDate?: string
}

export interface DiaryListResponse {
  content: Diary[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first: boolean
  last: boolean
}

export interface ErrorResponse {
  message: string
}
