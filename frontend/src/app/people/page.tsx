import { redirect } from 'next/navigation'
import PeoplePicker from './PeoplePicker'

// 메뉴를 고른 뒤 인원 수를 정하는 단계. 인원은 재료 양 계산에만 쓰이므로 여기서 처음 받는다.
export default async function PeoplePage({
  searchParams,
}: {
  searchParams: Promise<{ menuId?: string }>
}) {
  const { menuId } = await searchParams
  if (!menuId) redirect('/')
  return <PeoplePicker menuId={menuId} />
}
