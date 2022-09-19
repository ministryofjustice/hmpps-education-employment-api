import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Table(name = "work_readiness1")
@Entity
// @TypeDefs(TypeDef(name = "json", typeClass = JsonStringType::class))
data class ReadinessProfile1(
  @Id
  var offenderId: String,

  var bookingId: Long,

  /*var createdBy: String,

  var createdDateTime: LocalDateTime,

  var modifiedBy: String,

  var modifiedDateTime: LocalDateTime,

  var schemaVersion: String,

//  @Type(type = "json")
//  @Column(columnDefinition = "json")
  var profileData: String,

//  @Type(type = "json")
//  @Column(columnDefinition = "json")
  var notesData: String,

//  @Value("false")
  var new: String*/
) /*{
  constructor() : this("", "", -1, "", false)
  constructor(userId: String, offenderId: String, bookingId: Long, profile: String, isNew: Boolean) : this(

    offenderId = offenderId,
    bookingId = bookingId,
    createdBy = userId,
    createdDateTime = LocalDateTime.now(),
    modifiedBy = userId,
    modifiedDateTime = LocalDateTime.now(),
    schemaVersion = "1.0.0",
    profileData = profile,
    notesData = "[]",
    new = isNew
  )

}*/
